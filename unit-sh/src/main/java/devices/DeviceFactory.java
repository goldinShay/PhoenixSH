package devices;

import utils.NotificationService;

import java.time.Clock;

public class DeviceFactory {

    private static final Clock clock = Clock.systemDefaultZone();

    // 🌟 The method you need — based on DeviceType enum
    public static Device createDevice(DeviceType type, String id, String name) {
        switch (type) {
            case LIGHT:
                return new Light(id, name, clock);
            case DRYER:
                return new Dryer(id, name, clock);
            case WASHING_MACHINE:
                return new WashingMachine(id, name, clock);
            case THERMOSTAT:
                return new Thermostat(id, name, clock);
            // Add more cases as needed
            default:
                throw new IllegalArgumentException("Unsupported device type: " + type);
        }
    }

    // 🧭 Optional fallback for Excel loader etc.
    public static Device createDeviceByType(String typeName) {
        return createDeviceByType(typeName, "UNKNOWN_ID", "Unnamed Device");
    }

    public static Device createDeviceByType(String typeName, String id, String name) {
        try {
            DeviceType type = DeviceType.valueOf(typeName.toUpperCase());
            return createDevice(type, id, name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported device type: " + typeName);
        }
    }
}
