package storage.xlc;


import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import devices.SmartLight;
import devices.actions.SmartLightAction;
import devices.actions.SmartLightColorMode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DeviceStorage;
import storage.xlc.sheetsCommand.DeviceSheetCommand;
import utils.DeviceIdManager;
import utils.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import storage.xlc.sheetsCommand.XlTabNames;
import static storage.xlc.XlWorkbookUtils.updateWorkbook;
import static storage.xlc.XlWorkbookUtils.getFirstAvailableRow;
import devices.DeviceType;
import devices.actions.SmartLightColorMode;
import devices.DeviceDefaults;



// ... other imports

public class XlDeviceManager {
    private static final Path FILE_PATH = XlWorkbookUtils.getFilePath();
    private static final Clock clock = utils.ClockUtil.getClock();


    public static List<Device> loadDevicesFromExcel() {
        List<Device> devices = new ArrayList<>();
        Log.debug("📁 Loading devices from Excel file: " + FILE_PATH);

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(XlTabNames.DEVICES.value());
            if (sheet == null) {
                Log.warn("⚠️ Sheet '" + XlTabNames.DEVICES.value() + "' not found.");
                return devices;
            }

            for (Row row : sheet) {
                int rowIndex = row.getRowNum();
                if (rowIndex == 0) continue; // Skip header
                if (row == null || XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.TYPE.ordinal()).isBlank()) continue;

                try {
                    // 🧾 Log raw row for debug
                    String debugRow = Arrays.stream(DeviceSheetCommand.values())
                            .map(col -> XlWorkbookUtils.getCellValue(row, col.ordinal()))
                            .reduce((a, b) -> a + " | " + b).orElse("Empty row");
                    Log.debug("🔍 Row " + rowIndex + ": " + debugRow);

                    // 📦 Basic fields
                    String rawType = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.TYPE.ordinal()).trim();
                    DeviceType type = DeviceType.fromString(rawType);
                    if (type == null || type == DeviceType.UNKNOWN) {
                        throw new IllegalArgumentException("❌ Invalid or unsupported device type: " + rawType);
                    }

                    String id = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.ID.ordinal()).trim();
                    if (DeviceIdManager.getInstance().isIdTaken(id)) {
                        throw new IllegalArgumentException("❌ Duplicate ID in Excel: " + id);
                    }

                    String name = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.NAME.ordinal());
                    String brand = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.BRAND.ordinal());
                    String model = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.MODEL.ordinal());
                    String colorModeLabel = getCellValue(row, DeviceSheetCommand.COLOR_MODE.ordinal());

                    Cell enabledCell = row.getCell(DeviceSheetCommand.AUTO_ENABLED.ordinal());
                    boolean autoEnabled = parseBoolean(enabledCell);

                    double autoOn = XlWorkbookUtils.getSafeNumeric(
                            row.getCell(DeviceSheetCommand.AUTO_ON.ordinal()),
                            DeviceDefaults.getDefaultAutoOn(DeviceType.SMART_LIGHT));

                    double autoOff = XlWorkbookUtils.getSafeNumeric(
                            row.getCell(DeviceSheetCommand.AUTO_OFF.ordinal()),
                            DeviceDefaults.getDefaultAutoOff(DeviceType.SMART_LIGHT));


                    // 🛠️ Create device
                    Device device = DeviceFactory.createDeviceByType(type, id, name, clock, DeviceStorage.getDevices());
                    if (device == null) {
                        throw new IllegalStateException("❌ Device creation returned null for ID: " + id);
                    }

                    device.setBrand(brand);
                    device.setModel(model);
                    device.setAutomationEnabled(autoEnabled);

                    // 🌈 Color mode setup for SmartLights
                    if (device instanceof SmartLight sl && colorModeLabel != null && !colorModeLabel.isBlank()) {
                        sl.setColorMode(SmartLightColorMode.fromLabel(colorModeLabel));
                    }

                    Log.info("✅ Loaded device: " + device.getId() + " (" + device.getType() + ")");
                    devices.add(device);

                } catch (Exception ex) {
                    Log.warn("🚫 Failed to parse row " + rowIndex + ": " + ex.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("🛑 Excel read error: " + e.getMessage());
        }

        Log.info("📦 Total devices loaded: " + devices.size());
        return devices;
    }


    public static void writeDeviceToExcel(Device device) throws IOException {
        XlWorkbookUtils.updateWorkbook((tasks, sheet, sensors, senseControl, smartControl) -> {
            int rowNum = XlWorkbookUtils.getFirstAvailableRow(sheet);
            if (rowNum == -1) throw new IOException("Excel row limit reached");
            Row row = sheet.createRow(rowNum);
            writeDeviceRow(device, row);
            Log.info("📝 Device written: " + device.getId() + " [" + device.getType() + "]");
        });
    }

    private static void writeDeviceRow(Device device, Row row) {
        row.createCell(DeviceSheetCommand.TYPE.ordinal()).setCellValue(device.getType().name());
        row.createCell(DeviceSheetCommand.ID.ordinal()).setCellValue(device.getId());
        row.createCell(DeviceSheetCommand.NAME.ordinal()).setCellValue(device.getName());
        row.createCell(DeviceSheetCommand.BRAND.ordinal()).setCellValue(device.getBrand());
        row.createCell(DeviceSheetCommand.MODEL.ordinal()).setCellValue(device.getModel());
        row.createCell(DeviceSheetCommand.AUTO_ENABLED.ordinal()).setCellValue(device.isAutomationEnabled());
        row.createCell(DeviceSheetCommand.AUTO_ON.ordinal()).setCellValue(device.getAutoOnThreshold());
        row.createCell(DeviceSheetCommand.AUTO_OFF.ordinal()).setCellValue(device.getAutoOffThreshold());
        row.createCell(DeviceSheetCommand.ACTIONS.ordinal()).setCellValue(String.join(", ", device.getAvailableActions()));
        row.createCell(DeviceSheetCommand.ADDED_TS.ordinal()).setCellValue(device.getAddedTimestamp().toString());
        row.createCell(DeviceSheetCommand.UPDATED_TS.ordinal()).setCellValue(device.getUpdatedTimestamp().toString());
        row.createCell(DeviceSheetCommand.REMOVED_TS.ordinal()).setCellValue(device.getRemovedTimestamp());

        if (device instanceof SmartLight smart) {
            SmartLightAction mode = smart.getLightMode();
            if (mode != null) {
                row.createCell(DeviceSheetCommand.RGB_R.ordinal()).setCellValue(mode.getRed());
                row.createCell(DeviceSheetCommand.RGB_G.ordinal()).setCellValue(mode.getGreen());
                row.createCell(DeviceSheetCommand.RGB_B.ordinal()).setCellValue(mode.getBlue());
                row.createCell(DeviceSheetCommand.ACTIVE_MODE.ordinal()).setCellValue(mode.toString());
                Log.debug("🎨 Writing SmartLight RGB mode: " + mode);
            } else {
                row.createCell(DeviceSheetCommand.RGB_R.ordinal()).setCellValue(0);
                row.createCell(DeviceSheetCommand.RGB_G.ordinal()).setCellValue(0);
                row.createCell(DeviceSheetCommand.RGB_B.ordinal()).setCellValue(0);
                row.createCell(DeviceSheetCommand.ACTIVE_MODE.ordinal()).setCellValue("None");
                Log.debug("🎨 SmartLight has no RGB mode set");
            }
        }
    }
    private static String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
    private static boolean parseBoolean(Cell cell) {
        if (cell == null) return false;
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> Boolean.parseBoolean(cell.getStringCellValue().trim());
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> false;
        };
    }
    private static double getCellSafeNumeric(Cell cell, double fallback) {
        try {
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell != null && cell.getCellType() == CellType.STRING) {
                return Double.parseDouble(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            Log.warn("⚠️ Failed to parse numeric cell → " + e.getMessage());
        }
        return fallback;
    }
    public static boolean updateDevice(Device device) {
        return updateWorkbook((tasks, sheet, sensors, senseControl, smartControl) -> {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String rowId = getCellValue(row, DeviceSheetCommand.ID.ordinal());
                if (rowId.equalsIgnoreCase(device.getId())) {
                    row.getCell(DeviceSheetCommand.NAME.ordinal()).setCellValue(device.getName());
                    row.getCell(DeviceSheetCommand.BRAND.ordinal()).setCellValue(device.getBrand());
                    row.getCell(DeviceSheetCommand.MODEL.ordinal()).setCellValue(device.getModel());
                    row.getCell(DeviceSheetCommand.AUTO_ENABLED.ordinal()).setCellValue(device.isAutomationEnabled());
                    row.getCell(DeviceSheetCommand.AUTO_ON.ordinal()).setCellValue(device.getAutoOnThreshold());
                    row.getCell(DeviceSheetCommand.AUTO_OFF.ordinal()).setCellValue(device.getAutoOffThreshold());
                    row.getCell(DeviceSheetCommand.ACTIONS.ordinal()).setCellValue(String.join(", ", device.getAvailableActions()));
                    row.getCell(DeviceSheetCommand.UPDATED_TS.ordinal()).setCellValue(java.time.ZonedDateTime.now(clock).toString());

                    if (device instanceof SmartLight smart) {
                        SmartLightAction mode = smart.getLightMode();
                        if (mode != null) {
                            row.getCell(DeviceSheetCommand.RGB_R.ordinal()).setCellValue(mode.getRed());
                            row.getCell(DeviceSheetCommand.RGB_G.ordinal()).setCellValue(mode.getGreen());
                            row.getCell(DeviceSheetCommand.RGB_B.ordinal()).setCellValue(mode.getBlue());
                            row.getCell(DeviceSheetCommand.ACTIVE_MODE.ordinal()).setCellValue(mode.toString());
                            Log.debug("🎨 Updated SmartLight RGB → Mode: " + mode);
                        } else {
                            row.getCell(DeviceSheetCommand.RGB_R.ordinal()).setCellValue(0);
                            row.getCell(DeviceSheetCommand.RGB_G.ordinal()).setCellValue(0);
                            row.getCell(DeviceSheetCommand.RGB_B.ordinal()).setCellValue(0);
                            row.getCell(DeviceSheetCommand.ACTIVE_MODE.ordinal()).setCellValue("None");
                        }
                    }

                    Log.info("✅ Device updated in Excel: " + device.getId());
                    return;
                }
            }
            Log.warn("⚠️ Device not found during update: " + device.getId());
            throw new IOException("Device not found in sheet: " + device.getId());
        });
    }
    public static boolean removeDevice(String deviceId) {
        return updateWorkbook((tasks, sheet, sensors, senseControl, smartControl) -> {
            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String rowId = getCellValue(row, DeviceSheetCommand.ID.ordinal());
                if (rowId.equalsIgnoreCase(deviceId)) {
                    sheet.removeRow(row);
                    if (i < lastRow) {
                        sheet.shiftRows(i + 1, lastRow, -1);
                    }
                    Log.info("🗑️ Removed device from Excel: " + deviceId);
                    return;
                }
            }
            Log.warn("⚠️ Device not found during remove: " + deviceId);
            throw new IOException("Device not found in Excel sheet: " + deviceId);
        });
    }
    public static String getNextAvailableId(String prefix, Set<String> existingIds) {
        int max = existingIds.stream()
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.replace(prefix, ""))
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        String nextId = prefix + String.format("%03d", max + 1);
        Log.debug("🔢 Generated next device ID: " + nextId);
        return nextId;
    }

}
