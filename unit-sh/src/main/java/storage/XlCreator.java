package storage;

import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import devices.SmartLight;
import devices.actions.LiveDeviceState;
import org.apache.poi.ss.usermodel.*;
import sensors.Sensor;
import storage.xlc.*;
import storage.xlc.sheetsCommand.DeviceSheetCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


public class XlCreator {

    private static final XlDeviceManager deviceManager = new XlDeviceManager();
    private static final XlSensorManager sensorManager = new XlSensorManager();
    private static final XlAutoOpManager XlAutoOpManager = new XlAutoOpManager();
    private static final XlTaskSchedulerManager schedulerManager = new XlTaskSchedulerManager();
    private static final XlSmartLightManager smartLightManager = new XlSmartLightManager();
    private static final String SENS_CTRL = "Sens_Ctrl";


    // 🧪 Optional test stubs
    private static Function<Device, Boolean> deviceUpdater = null;
    private static Function<String, Boolean> deviceRemover = null;
    private static Function<Sensor, Boolean> sensorUpdater = null;
    private static Function<DeviceSensorPair, Boolean> senseAppender = null;
    private static Consumer<String> senseRemover = slaveId -> {
        // your removal logic here
        XlAutoOpManager.removeSensorLink(slaveId);
    };

    // ----- Hook Injectors -----
    public static void setDeviceUpdater(Function<Device, Boolean> f) { deviceUpdater = f; }
    public static void setDeviceRemover(Function<String, Boolean> f) { deviceRemover = f; }
    public static void setSensorUpdater(Function<Sensor, Boolean> f) { sensorUpdater = f; }

    // ----- Device Delegates -----
    public static List<Device> loadDevicesFromExcel() {
        List<Device> devices = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        File file = new File(config.SystemPaths.EXCEL_FILE_PATH);
        System.out.println("📂 Reading Excel file from: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.err.println("❌ Excel file not found: " + file.getAbsolutePath());
            return Collections.emptyList();
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // 🎯 Load Devices sheet
            Sheet sheet = workbook.getSheet("Devices");
            if (sheet == null) {
                System.err.println("🚫 Sheet 'Devices' not found! Available sheets:");
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    System.out.println("📄 Sheet[" + i + "]: " + workbook.getSheetName(i));
                }
                return Collections.emptyList();
            }

            // 📊 Map header columns
            Map<DeviceSheetCommand, Integer> columnMap = getColumnIndexMap(sheet);
            if (!columnMap.containsKey(DeviceSheetCommand.TYPE) || !columnMap.containsKey(DeviceSheetCommand.DEVICE_ID)) {
                System.err.println("❌ Missing required columns in 'Devices' sheet.");
                return Collections.emptyList();
            }

            // 🚦 Parse rows from Devices sheet
            for (Row row : sheet) {
                int rowIndex = row.getRowNum();
                if (rowIndex == 0 || row == null) continue;

                String typeStr = getCellValue(row, columnMap.get(DeviceSheetCommand.TYPE)).trim();
                String rawId   = getCellValue(row, columnMap.get(DeviceSheetCommand.DEVICE_ID)).trim();

                if (typeStr.isEmpty() || rawId.isEmpty()) {
                    System.err.println("⚠️ Row " + rowIndex + " missing type or ID. Skipping.");
                    continue;
                }

                DeviceType type = DeviceType.fromString(typeStr);
                if (type == DeviceType.SMART_LIGHT) {
                    System.out.println("🚫 Row " + rowIndex + " skipped (SmartLight handled separately).");
                    continue;
                }

                if (seenIds.contains(rawId)) {
                    System.err.println("❌ Duplicate ID found in row " + rowIndex + ": " + rawId);
                    continue;
                }

                seenIds.add(rawId);

                try {
                    Device device = parseDeviceRow(row, columnMap);
                    if (device != null) {
                        devices.add(device);
                        DeviceStorage.getDevices().put(device.getId(), device);
                    } else {
                        System.err.println("⚠️ Row " + rowIndex + " failed to create a device.");
                    }
                } catch (Exception ex) {
                    System.err.println("🚫 Error parsing row " + rowIndex + ": " + ex.getMessage());
                }
            }

            // 🌟 Load SmartLights from dedicated sheet
            Map<String, SmartLight> smartLights = XlSmartLightManager.loadSmartLights();
            smartLights.values().forEach(light -> {
                String id = light.getId();
                if (!DeviceStorage.getDevices().containsKey(id)) {
                    DeviceStorage.getDevices().put(id, light);
                    devices.add(light);
                } else {
                    System.err.println("⚠️ SmartLight already in memory, skipping: " + id);
                }
            });

            System.out.println("📦 Total devices loaded into memory: " + devices.size());

        } catch (IOException e) {
            System.err.println("📛 Failed to read Excel file: " + e.getMessage());
        }
        // 🔁 Sync live state from loaded devices
        devices.forEach(device -> {
            if (device.isOn()) {
                LiveDeviceState.turnOn(device);
            } else {
                LiveDeviceState.turnOff(device);
            }
        });


        return devices;
    }

    public static boolean delegateDeviceUpdate(Device device) {

        try {
            boolean result;

            if (deviceUpdater != null) {
                result = deviceUpdater.apply(device);
                System.out.println("🧪 updateDevice → using deviceUpdater, success: " + result);
            } else {
                result = deviceManager.updateDevice(device);
                System.out.println("🧪 updateDevice → using deviceManager, success: " + result);
            }
            System.out.println("🧪 XlCreator.applyDeviceUpdate → using deviceManager, success: " + result);

            return result;

        } catch (Exception e) {
            System.err.println("💥 XlCreator.updateDevice → Exception during update: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeDevice(String deviceId) {
        return (deviceRemover != null) ? deviceRemover.apply(deviceId)
                : deviceManager.removeDevice(deviceId);
    }

    // ----- Sensor Delegates -----
    public static Map<String, Sensor> loadSensors() {
        return sensorManager.loadSensors();
    }

    public static boolean writeSensorToExcel(Sensor sensor) {
        return sensorManager.writeSensorToExcel(sensor);
    }

    public static boolean updateSensor(Sensor sensor) {
        return (sensorUpdater != null) ? sensorUpdater.apply(sensor)
                : XlSensorManager.updateSensorControlLink(sensor);
    }

    public static boolean removeSensor(String sensorId) {
        return sensorManager.removeSensor(sensorId);
    }

    // ----- Sense Control Delegates -----
    public static boolean appendToAutoOpManager(Device slave, Sensor master) {
        return (senseAppender != null) ? senseAppender.apply(new DeviceSensorPair(slave, master))
                : XlAutoOpManager.appendToSensCtrlSheet(slave, master);
    }

    public static void removeFromSensCtrl(String slaveId) {
        if (senseRemover != null) {
            senseRemover.accept(slaveId);
        } else {
            XlAutoOpManager.removeSensorLink(slaveId);
        }
    }


    public boolean updateAutoOpThresholds(String deviceId, double unifiedThreshold, double ignoredOff) {
        // Feed same value for both ON and OFF fields
        return XlAutoOpManager.updateAutoOpThresholds(deviceId, unifiedThreshold, unifiedThreshold);
    }

    public static void removeSensorLink(String slaveId) {
        XlAutoOpManager.removeSensorLink(slaveId);
    }

    public static boolean createNewWorkbook() {
        return XlWorkbookUtils.ensureFileExists();
    }

    // 🔗 Simple pair holder for test stubbing
    public static class DeviceSensorPair {
        public final Device slave;
        public final Sensor master;

        public DeviceSensorPair(Device slave, Sensor master) {
            this.slave = slave;
            this.master = master;
        }
    }
    public static Device parseDeviceRow(Row row, Map<DeviceSheetCommand, Integer> columnMap) {
        if (row == null) return null;

        try {
            int rowIndex = row.getRowNum();

            // 🧠 Extract raw values from Excel
            String typeStr    = getCellValue(row, columnMap.get(DeviceSheetCommand.TYPE)).trim();
            String id         = getCellValue(row, columnMap.get(DeviceSheetCommand.DEVICE_ID)).trim();
            String name       = getCellValue(row, columnMap.get(DeviceSheetCommand.NAME)).trim();
            String brand      = getCellValue(row, columnMap.get(DeviceSheetCommand.BRAND)).trim();
            String model      = getCellValue(row, columnMap.get(DeviceSheetCommand.MODEL)).trim();
            String autoEnable = getCellValue(row, columnMap.get(DeviceSheetCommand.AUTO_ENABLED)).trim();
            String stateStr   = getCellValue(row, columnMap.get(DeviceSheetCommand.STATE)).trim();

            // 🧠 Parse device type
            DeviceType type = DeviceType.fromString(typeStr);
            if (type == DeviceType.UNKNOWN) {
                System.err.println("🚫 Row " + rowIndex + ": Invalid device type '" + typeStr + "'");
                return null;
            }

            // 🛠️ Create device instance
            Device device = DeviceFactory.createDeviceByType(
                    type,
                    id,
                    name,
                    Clock.systemDefaultZone(),
                    DeviceStorage.getDevices(),
                    brand,
                    model
            );

            if (device != null) {
                // 🧬 Set brand and model
                device.setBrand(brand);
                device.setModel(model);

                // ⚙️ Set automation flag
                boolean automationEnabled = "1".equals(autoEnable) || "true".equalsIgnoreCase(autoEnable);
                device.setAutomationEnabled(automationEnabled);

                // 🔌 Set ON/OFF state from Excel
                boolean isOn = "ON".equalsIgnoreCase(stateStr);
                device.setState(isOn);

                // 🧪 Optional debug logging
                // System.out.println("[DEBUG] Row " + rowIndex + " | Device ID: " + id + " | isOn: " + isOn);
            } else {
                System.err.println("⚠️ Row " + rowIndex + ": Failed to instantiate device.");
            }

            return device;

        } catch (Exception e) {
            System.err.println("❌ Failed to parse device row at index " + row.getRowNum() + ": " + e.getMessage());
            return null;
        }
    }

    private static String getCellValue(Row row, Integer index) {
        if (index == null) return "";
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
    public static Map<DeviceSheetCommand, Integer> getColumnIndexMap(Sheet sheet) {
        Map<DeviceSheetCommand, Integer> indexMap = new HashMap<>();
        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            System.err.println("❌ Missing header row (Row 0) in sheet: " + sheet.getSheetName());
            return indexMap;
        }

        for (Cell cell : headerRow) {
            String rawHeader = cell.getStringCellValue();
//            System.out.println("🔍 Found header cell[" + cell.getColumnIndex() + "]: '" + rawHeader + "'");

            if (rawHeader == null || rawHeader.isBlank()) continue;

            String normalizedHeader = normalizeHeader(rawHeader);

            for (DeviceSheetCommand cmd : DeviceSheetCommand.values()) {
                String expectedHeader = normalizeHeader(cmd.label());
//                System.out.println("🔬 Comparing '" + normalizedHeader + "' with expected '" + expectedHeader + "' for " + cmd);

                if (normalizedHeader.equals(expectedHeader)) {
                    indexMap.put(cmd, cell.getColumnIndex());
                    break;
                }
            }
        }

        if (indexMap.isEmpty()) {
            System.err.println("⚠️ No columns were mapped — check header row formatting.");
        }

        return indexMap;
    }

    private static String normalizeHeader(String input) {
        return input.trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }
}
