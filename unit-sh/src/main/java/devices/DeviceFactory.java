package devices;

import devices.actions.SmartLightColorMode;
import devices.actions.SmartLightEffect;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.xlc.XlWorkbookUtils;
import storage.xlc.sheetsCommand.DeviceSheetCommand;
import utils.Log;
import utils.NotificationService;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

public class DeviceFactory {

    private static final Map<String, Device> devices = new HashMap<>();
    private static DeviceCreator overrideDeviceCreator;

    public static void setDeviceCreator(DeviceCreator creator) {
        overrideDeviceCreator = creator;
    }

    public static Device createDevice(
            DeviceType type,
            String id,
            String name,
            Clock clock,
            Map<String, Device> allDevices
    ) {
        boolean skipIdCheck = (allDevices == null);

        if (overrideDeviceCreator != null) {
            return overrideDeviceCreator.create(id, name, clock, allDevices);
        }

        switch (type) {
            case LIGHT -> {
                boolean savedState = getSavedState(id);
                double autoOn = 1024.0, autoOff = 1050.0;
                boolean autoEnabled = false;

                try (Workbook workbook = new XSSFWorkbook(new FileInputStream(XlWorkbookUtils.getFilePath().toFile()))) {
                    Sheet sheet = workbook.getSheet("Devices");
                    if (sheet != null) {
                        for (Row row : sheet) {
                            if (row.getRowNum() == 0) continue;
                            if (Objects.equals(row.getCell(1).getStringCellValue(), id)) {
                                autoOn = Optional.ofNullable(row.getCell(6)).map(Cell::getNumericCellValue).orElse(autoOn);
                                autoOff = Optional.ofNullable(row.getCell(7)).map(Cell::getNumericCellValue).orElse(autoOff);

                                Cell autoCell = row.getCell(5);
                                autoEnabled = (autoCell != null && switch (autoCell.getCellType()) {
                                    case BOOLEAN -> autoCell.getBooleanCellValue();
                                    case STRING -> Boolean.parseBoolean(autoCell.getStringCellValue().trim());
                                    default -> false;
                                });
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.error("🛑 Failed to read thresholds for LIGHT [" + id + "]: " + e.getMessage());
                }

                Light light = new Light(id, name, clock, savedState, autoOn, autoOff, skipIdCheck);
                light.setAutomationEnabled(autoEnabled);
                return light;
            }

            case SMART_LIGHT -> {
                boolean savedState = getSavedState(id);
                double autoOn = DeviceDefaults.getDefaultAutoOn(DeviceType.SMART_LIGHT);
                double autoOff = DeviceDefaults.getDefaultAutoOff(DeviceType.SMART_LIGHT);
                boolean autoEnabled = false;
                String brand = "Unknown", model = "Unknown";
                String colorModeLabel = null;

                try (Workbook workbook = new XSSFWorkbook(new FileInputStream(XlWorkbookUtils.getFilePath().toFile()))) {
                    Sheet sheet = workbook.getSheet("Devices");
                    if (sheet != null) {
                        for (Row row : sheet) {
                            if (row.getRowNum() == 0) continue;
                            if (Objects.equals(XlWorkbookUtils.getCellValue(row, 1).trim(), id)) {
                                autoOn = Optional.ofNullable(row.getCell(6)).map(Cell::getNumericCellValue).orElse(autoOn);
                                autoOff = Optional.ofNullable(row.getCell(7)).map(Cell::getNumericCellValue).orElse(autoOff);

                                autoEnabled = Optional.ofNullable(row.getCell(5)).map(cell -> switch (cell.getCellType()) {
                                    case BOOLEAN -> cell.getBooleanCellValue();
                                    case STRING -> Boolean.parseBoolean(cell.getStringCellValue().trim());
                                    default -> false;
                                }).orElse(false);

                                brand = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.BRAND.ordinal());
                                model = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.MODEL.ordinal());
                                colorModeLabel = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.COLOR_MODE.ordinal());
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.error("🛑 Failed to read SmartLight parameters for ID [" + id + "]: " + e.getMessage());
                }

                SmartLight smartLight = new SmartLight(id, name, brand, model, clock, savedState, autoOn, autoOff, skipIdCheck);
                smartLight.setAutomationEnabled(autoEnabled);
                smartLight.setColorMode(
                        (colorModeLabel != null && !colorModeLabel.isBlank())
                                ? SmartLightColorMode.fromLabel(colorModeLabel)
                                : SmartLightColorMode.WARM_WHITE);

                smartLight.applyEffect(SmartLightEffect.NONE);

                return smartLight;
            }




            case DRYER, WASHING_MACHINE -> {
                String brand = "Generic";
                String model = "Default";

                try (Workbook workbook = new XSSFWorkbook(new FileInputStream(XlWorkbookUtils.getFilePath().toFile()))) {
                    Sheet sheet = workbook.getSheet("Devices");
                    if (sheet != null) {
                        for (Row row : sheet) {
                            if (row.getRowNum() == 0) continue;
                            if (Objects.equals(row.getCell(1).getStringCellValue(), id)) {
                                brand = Optional.ofNullable(row.getCell(3)).map(Cell::getStringCellValue).orElse(brand);
                                model = Optional.ofNullable(row.getCell(4)).map(Cell::getStringCellValue).orElse(model);
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.warn("⚠️ Failed to load brand/model for [" + type + "] " + id + ": " + e.getMessage());
                }

                return switch (type) {
                    case DRYER -> new Dryer(id, name, brand, model, clock,
                            false,              // isOn (default: off)
                            1024.0,             // autoOnThreshold
                            1050.0,             // autoOffThreshold
                            skipIdCheck);
                    case WASHING_MACHINE -> new WashingMachine(id, name, brand, model, clock,
                            false,     // isOn
                            1024.0,    // autoOnThreshold
                            1050.0,    // autoOffThreshold
                            skipIdCheck);
                    default -> throw new IllegalStateException("Unexpected type: " + type);
                };
            }

            case THERMOSTAT -> {
                double defaultTemp = 25.0;
                NotificationService ns = new NotificationService();
                return new Thermostat(id, name, defaultTemp, ns, clock, skipIdCheck);
            }

            default -> throw new IllegalArgumentException("❌ Unknown device type: " + type);
        }
    }

    public static Device createDeviceByType(DeviceType type, String id, String name, Clock clock, Map<String, Device> existingDevices) {
        Log.debug("🔍 DeviceFactory - Enum Type: [" + type + "]");
        try {
            return createDevice(type, id, name, clock, existingDevices);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("❌ Failed to create device for type: " + type, e);
        }
    }


    public static boolean getSavedState(String deviceId) {
        Device device = devices.get(deviceId);
        return device != null && device.isOn();
    }

    public static Map<String, Device> getDevices() {
        return devices;
    }

    @FunctionalInterface
    public interface DeviceCreator {
        Device create(String id, String name, Clock clock, Map<String, Device> devices);
    }
}
