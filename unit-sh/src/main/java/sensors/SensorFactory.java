package sensors;

import java.time.Clock;
import java.util.*;

public class SensorFactory {

    private static final Clock clock = Clock.systemDefaultZone();
    private static final Map<String, Sensor> sensors = new HashMap<>();
    private static SensorCreator overrideCreator = null;

    public static void setSensorCreator(SensorCreator customCreator) {
        overrideCreator = customCreator;
    }

    public static void resetSensorCreator() {
        overrideCreator = null;
    }


    // 🌟 Main factory method
    public static Sensor createSensor(
            SensorType type,
            String id,
            String name,
            String unit,
            int defaultValue,
            Clock clock
    ) {
        if (overrideCreator != null) {
            return overrideCreator.create(type, id, name, unit, defaultValue, clock);
        }

        return switch (type) {
            case LIGHT -> new LightSensor(id, name, unit, defaultValue, clock);
            // Future sensor types go here:
            // case TEMPERATURE -> return new TemperatureSensor(...);
            // case MOTION -> return new MotionSensor(...);
            default -> throw new IllegalArgumentException("Unsupported sensor type: " + type);
        };
    }


    // 🧭 Convenience wrapper using string type name (e.g., from Excel)
    public static Sensor createSensorByType(
            String typeName,
            String id,
            String name,
            String unit,
            int defaultValue,
            Clock clock
    ) {
        try {
            SensorType type = SensorType.valueOf(typeName.toUpperCase().trim());
            System.out.println("🔍 SensorFactory - Creating sensor of type: " + type);
            return createSensor(type, id, name, unit, defaultValue, clock);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported sensor type: " + typeName);
        }
    }

    // 📦 Optional registry for runtime lookup (e.g., for GUI links)
    public static void registerSensor(Sensor sensor) {
        sensors.put(sensor.getSensorId(), sensor);
    }

    public static Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

}
