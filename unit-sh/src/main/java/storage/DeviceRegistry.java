package storage;

import devices.Device;
import devices.DeviceType;
import devices.DeviceFactory;
import sensors.Sensor;
import sensors.SensorFactory;
import sensors.SensorType;
import utils.DeviceIdManager;

import java.io.IOException;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DeviceRegistry {

    private static final Clock clock = Clock.systemDefaultZone();

    /**
     * Returns all devices currently loaded from Excel.
     */
    public static List<Device> getAllDevices() {
        return XlCreator.loadDevicesFromExcel();
    }

    /**
     * Checks if a device name is already taken (case-insensitive).
     */
    public static boolean isNameTaken(String name) {
        return getAllDevices().stream()
                .anyMatch(device -> device.getName().equalsIgnoreCase(name.trim()));
    }

    /**
     * Generates a new ID based on the given DeviceType.
     */
    public static String generateId(DeviceType type) {
        return DeviceIdManager.getInstance().generateIdForType(type);
    }

    /**
     * Attempts to register and persist a new device.
     * Returns true if saved successfully, false otherwise.
     */
    public static boolean registerDevice(DeviceType type, Optional<SensorType> subtype, String name, String brand, String model) {
        name = name.trim(); // 🧼 Normalize input early

        // ✅ Check name in correct registry based on type
        if (type == DeviceType.SENSOR) {
            if (SensorStorage.isNameTaken(name)) {
                System.err.println("❌ Duplicate sensor name detected: " + name);
                return false;
            }

            if (subtype.isEmpty()) {
                System.err.println("❌ Missing SensorType for SENSOR device: " + name);
                return false;
            }

            SensorType sensorType = subtype.get();
            String id = DeviceIdManager.getInstance().generateIdForSensorType(sensorType);

            try {
                Sensor sensor = SensorFactory.createSensor(sensorType, id, name, clock);

                SensorStorage.register(sensor);               // ✅ Use new registry method
                XlCreator.writeSensorToExcel(sensor);         // Persist sensor info

                System.out.println("📡 Sensor registered: " + name + " → ID: " + id);
                return true;
            } catch (Exception e) {
                System.err.println("❌ Failed to register sensor: " + e.getMessage());
                return false;
            }
        }

        // ✅ Device branch with name check
        if (isNameTaken(name)) {
            System.err.println("❌ Duplicate device name detected: " + name);
            return false;
        }

        String id = generateId(type);

        try {
            Device newDevice = DeviceFactory.createDeviceByType(type, id, name, clock, DeviceStorage.getDevices());
            if (newDevice == null) {
                System.err.println("❌ Failed to create device of type: " + type);
                return false;
            }

            newDevice.setBrand(brand);
            newDevice.setModel(model);

            DeviceStorage.add(newDevice);                    // Add to registry
            XlCreator.writeDeviceToExcel(newDevice);         // Persist device info

            System.out.println("✅ Device registered: " + name + " → ID: " + id);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Excel write failed: " + e.getMessage());
            return false;
        }
    }



}
