package devices;

import devices.actions.ApprovedDeviceModel;
import devices.actions.SmartLightColorMode;
import devices.actions.SmartLightEffect;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.xlc.XlSmartLightManager;
import storage.xlc.XlWorkbookUtils;
import storage.xlc.sheetsCommand.DeviceSheetCommand;
import storage.xlc.sheetsCommand.SmartLightSheetCommand;
import utils.Log;
import utils.NotificationService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

public class DeviceFactory {

    private static final Map<String, Device> devices = new HashMap<>();
    private static DeviceCreator overrideDeviceCreator;

    public static void setDeviceCreator(DeviceCreator creator) {
        overrideDeviceCreator = creator;
    }
//TODO: carve createDevice, place holder for model: GENERIC, add brand and model to all device types
public static Device createDevice(
        DeviceType type,
        String id,
        String name,
        Clock clock,
        Map<String, Device> allDevices,
        ApprovedDeviceModel approvedModel,
        String brand,
        String model
) {
    boolean skipIdCheck = true;

    if (overrideDeviceCreator != null) {
        return overrideDeviceCreator.create(id, name, clock, allDevices);
    }

    // ✅ Validate model
    if (ApprovedDeviceModel.lookup(brand, model) == null) {
        throw new IllegalArgumentException("Device not approved: " + brand + " " + model);
    }

    double autoOn = 1024.0;
    double autoOff = 1050.0;
    boolean autoEnabled = false;

    // 📥 Load thresholds and automation flags from Excel
    try (Workbook workbook = WorkbookFactory.create(new FileInputStream(XlWorkbookUtils.getFilePath().toFile()))) {
        Sheet sheet = workbook.getSheet("Devices");
        if (sheet != null) {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String sheetId = XlWorkbookUtils.getCellValue(row, DeviceSheetCommand.DEVICE_ID.ordinal()).trim();
                if (!id.equals(sheetId)) continue;

                autoOn = Optional.ofNullable(row.getCell(DeviceSheetCommand.AUTO_ON.ordinal()))
                        .map(Cell::getNumericCellValue).orElse(autoOn);

                autoOff = Optional.ofNullable(row.getCell(DeviceSheetCommand.AUTO_OFF.ordinal()))
                        .map(Cell::getNumericCellValue).orElse(autoOff);

                autoEnabled = Optional.ofNullable(row.getCell(DeviceSheetCommand.AUTO_ENABLED.ordinal()))
                        .map(cell -> switch (cell.getCellType()) {
                            case BOOLEAN -> cell.getBooleanCellValue();
                            case STRING -> Boolean.parseBoolean(cell.getStringCellValue().trim());
                            default -> false;
                        }).orElse(false);

                // 🧠 Fallback brand/model from sheet if missing
                brand = Optional.ofNullable(row.getCell(DeviceSheetCommand.BRAND.ordinal()))
                        .map(Cell::getStringCellValue).orElse(brand);

                model = Optional.ofNullable(row.getCell(DeviceSheetCommand.MODEL.ordinal()))
                        .map(Cell::getStringCellValue).orElse(model);

                break;
            }
        }
    } catch (IOException e) {
        Log.warn("⚠️ Failed to read device sheet: " + e.getMessage());
    }

    // ✅ Final model validation
    ApprovedDeviceModel resolvedModel = ApprovedDeviceModel.lookup(brand, model);
    if (resolvedModel == null) {
        throw new IllegalArgumentException("❌ Device creation failed: Unapproved model → " + brand + " / " + model);
    }

    // 🎯 Create device by type
    Device device = switch (type) {
        case LIGHT -> {
            boolean savedState = getSavedState(id);
            Light light = new Light(id, name, clock, savedState, autoOn, autoOff, skipIdCheck);
            light.setAutomationEnabled(autoEnabled);
            yield light;
        }

        case SMART_LIGHT -> {
            boolean savedState = getSavedState(id);
            SmartLight smartLight = new SmartLight(id, name, resolvedModel, clock, savedState, autoOn, autoOff, skipIdCheck);
            smartLight.setAutomationEnabled(autoEnabled);
            // 🌈 Optional: write config if needed
            XlSmartLightManager.writeSmartLight(smartLight);
            yield smartLight;
        }

        case DRYER -> new Dryer(id, name, brand, model, clock, false, autoOn, autoOff, skipIdCheck);
        case WASHING_MACHINE -> new WashingMachine(id, name, brand, model, clock, false, autoOn, autoOff, skipIdCheck);
        case THERMOSTAT -> {
            NotificationService ns = new NotificationService();
            yield new Thermostat(id, name, 25.0, ns, clock, skipIdCheck);
        }

        default -> throw new IllegalArgumentException("❌ Unknown device type: " + type);
    };

    // 🧾 Apply brand/model to all devices
    device.setBrand(brand);
    device.setModel(model);

    return device;
}

    public static Device createDeviceByType(
            DeviceType type,
            String id,
            String name,
            Clock clock,
            Map<String, Device> existingDevices,
            String brand,
            String model
    ) {
        Log.debug("🔍 DeviceFactory - Enum Type: [" + type + "]");
        Log.debug("🧪 Brand/Model for device: [" + brand + "] / [" + model + "]");

        ApprovedDeviceModel approvedModel = ApprovedDeviceModel.lookup(brand, model);

        if (approvedModel == null) {
            Log.warn("❌ No approved model found for: " + brand + " / " + model + " — proceeding without strict enforcement.");
            // Optionally: try sanitizing inputs or log raw Excel values
        }

        try {
            return createDevice(type, id, name, clock, existingDevices, approvedModel, brand, model);
        } catch (IllegalArgumentException e) {
            Log.error("💥 Device creation failed: " + e.getMessage());
            return null; // allow recovery upstream
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
