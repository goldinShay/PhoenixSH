package devices;

import utils.NotificationService;

import java.time.Clock;

public class DeviceFactory {

    private static final Clock clock = Clock.systemDefaultZone();

    // ✨ New method for Excel loader use
    public static Device createDeviceByType(String type) {
        return createDeviceByType(type, "UNKNOWN_ID", "Unnamed Device");
    }

    // ✨ New overloaded method for proper instantiation
    public static Device createDeviceByType(String type, String id, String name) {
        switch (type.toLowerCase()) {
            case "light":
                return new Light(id, name, clock);
            case "dryer":
                return new Dryer(id, name, clock);
            case "washingmachine":
                return new WashingMachine(id, name, clock);
            case "thermostat":
                return new Thermostat(id, name, clock);
//            case "light-sensor":
//                return new LightSensor(id, name, clock); // if you have this class
            default:
                throw new IllegalArgumentException("Unsupported device type: " + type);
        }
    }

    // Existing method used elsewhere (e.g., string-parsing fallback)
    public static Device fromDataString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 3) return null;

        String type = parts[0];
        String id = parts[1];
        String name = parts[2];

        return createDeviceByType(type, id, name);
    }

    // NotificationService variant remains for special system use
    public static Device createDevice(String type, NotificationService ns) {
        return switch (type.toLowerCase()) {
            case "thermostat" -> new Thermostat(20.0, 22.0, "T999", ns, clock);
            default -> throw new IllegalArgumentException("Unsupported device type: " + type);
        };
    }
}
