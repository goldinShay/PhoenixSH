package devices;
import utils.NotificationService;

import java.time.Clock;

public class DeviceFactory {

    public static Device createDevice(String type, String id, String name) {
        Clock clock = Clock.systemDefaultZone(); // Central clock reference

        switch (type.toLowerCase()) {
            case "light":
                return new Light(id, name, clock);
            case "dryer":
                return new Dryer(id, name, clock);
            case "washingmachine":
                return new WashingMachine(id, name, clock);
            case "thermostat":
                return new Thermostat(id, name, clock);
            default:
                return new GenericDevice(id, name, type, clock);
        }
    }

    public static Device fromDataString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 3) return null;

        String type = parts[0];
        String id = parts[1];
        String name = parts[2];

        return createDevice(type, id, name);
    }
    public static Device createDevice(String type, NotificationService ns) {
        Clock clock = Clock.systemDefaultZone();

        return switch (type.toLowerCase()) {
            case "thermostat" -> // Correct order: temperature, threshold, id, notificationService, clock
                    new Thermostat(20.0, 22.0, "T999", ns, clock);
            // Add other types if they need auto-generated IDs
            default -> throw new IllegalArgumentException("Unsupported device type: " + type);
        };
    }

}
