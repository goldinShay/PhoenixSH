package sensors;

import utils.DeviceIdManager;
import utils.Log;

import java.time.Clock;
import java.util.*;

/**
 * A centralized factory and registry for sensor creation and lookup.
 */
public class SensorFactory {

    private static final Map<String, Sensor> registry = new HashMap<>();
    private static SensorCreator overrideCreator = null;

    public static void setSensorCreator(SensorCreator customCreator) {
        overrideCreator = customCreator;
    }

    public static void resetSensorCreator() {
        overrideCreator = null;
    }

    /**
     * Creates a sensor instance based on its type and configuration.
     */
    public static Sensor createSensor(SensorType type, String id, String name, Clock clock) {
        Objects.requireNonNull(type, "SensorType cannot be null");
        Objects.requireNonNull(name, "Sensor name cannot be null");

        name = name.trim();
        if (id == null || id.isBlank()) {
            id = DeviceIdManager.getInstance().generateIdForSensorType(type);
        }

        String unit = type.getDefaultUnit();
        int defaultValue = (int) type.getDefaultValue();

        if (overrideCreator != null) {
            return overrideCreator.create(type, id, name, unit, defaultValue, clock);
        }

        Sensor sensor;
        switch (type) {
            case LIGHT       -> sensor = new LightSensor(id, name, unit, defaultValue, clock);
//            case TEMPERATURE -> sensor = new TemperatureSensor(id, name, unit, defaultValue, clock);
//            case MOTION      -> sensor = new MotionSensor(id, name, unit, defaultValue, clock);
//            case DISTANCE    -> sensor = new DistanceSensor(id, name, unit, defaultValue, clock);
            // 🔮 Add additional sensor mappings here
            default -> {
                Log.warn("🚫 Unmapped SensorType: " + type);
                throw new IllegalArgumentException("Unsupported sensor type: " + type);
            }
        }

        return sensor;
    }

    /**
     * Convenience creation using a string name (e.g., from Excel).
     */
    public static Sensor createSensorByType(String typeName, String id, String name, Clock clock) {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException("SensorType name cannot be empty.");
        }

        try {
            SensorType type = SensorType.valueOf(typeName.trim().toUpperCase());
            Log.debug("🔍 SensorFactory - Creating sensor of type: " + type);
            return createSensor(type, id, name, clock);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported sensor type: " + typeName);
        }
    }

    // 🧭 Registry Functions

    public static void registerSensor(Sensor sensor) {
        if (sensor == null || sensor.getSensorId() == null || sensor.getSensorId().isBlank()) {
            Log.warn("⚠️ Attempted to register invalid sensor.");
            return;
        }
        registry.put(sensor.getSensorId(), sensor);
    }

    public static Sensor getSensor(String id) {
        return registry.get(id);
    }

    public static Map<String, Sensor> getAllSensors() {
        return Collections.unmodifiableMap(registry);
    }

    public static void clearRegistry() {
        registry.clear();
        Log.debug("🧹 SensorFactory registry cleared.");
    }
}
