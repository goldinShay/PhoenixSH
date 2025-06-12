package devices;

import storage.DeviceStorage;
import storage.XlCreator;
import utils.NotificationService;

import java.time.Clock;
import java.util.*;


public class DeviceFactory {

    private static final Clock clock = Clock.systemDefaultZone();

    // 🌟 The method you need — based on DeviceType enum
    public static Device createDevice(
            DeviceType type,
            String id,
            String name,
            Clock clock,
            Map<String, Device> allDevices
    ) {
        switch (type) {
            case LIGHT -> {
                Set<String> allIds = new HashSet<>(DeviceStorage.getDevices().keySet());  // ✅ Retrieve existing IDs
                String newId = XlCreator.getNextAvailableId("LI", allIds);  // ✅ Generate unique ID
                boolean savedState = getSavedState(newId); // ✅ Retrieve last known state
                return new Light(newId, name, clock, savedState); // ✅ Pass state into constructor

            }
            case DRYER -> throw new UnsupportedOperationException("Dryer support coming soon!"); // ✅ Properly disables Dryer
            case WASHING_MACHINE -> throw new UnsupportedOperationException("Washing Machine support coming soon!");
            case THERMOSTAT -> throw new UnsupportedOperationException("Thermostat support coming soon!");
            default -> throw new IllegalArgumentException("Unsupported device type: " + type);
        }

    }

    public static boolean getSavedState(String deviceId) {
        Device device = devices.get(deviceId);
        if (device != null) {
            System.out.println("🔍 Debug - Retrieving saved state: " + deviceId + " → " + device.isOn());
            return device.isOn(); // ✅ Pulls last known state
        }
        return false; // ✅ Defaults to OFF if no prior state exists
    }



    // 🧭 Optional fallback for Excel loader etc.
    public static Device createDeviceByType(String typeName) {
        return createDeviceByType(
                typeName,
                "UNKNOWN_ID",
                "Unnamed Device",
                Clock.systemDefaultZone(),                    // ⏰ Default clock
                DeviceFactory.getDevices()                    // 🗺️ Default device map
        );
    }


    public static Device createDeviceByType(String typeName, String id, String name, Clock clock, Map<String, Device> allDevices) {
        try {
            DeviceType type = DeviceType.fromString(typeName); // ✅ now uses your tested logic
            System.out.println("🔍 DeviceFactory - Processing Type: '" + type + "' (Expected Types: " + Arrays.toString(DeviceType.values()) + ")");
            return createDevice(type, id, name, clock, allDevices);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported device type: " + typeName);
        }
    }


    private static final Map<String, Device> devices = new HashMap<>();

    public static Map<String, Device> getDevices() {
        return devices;
    }

}
