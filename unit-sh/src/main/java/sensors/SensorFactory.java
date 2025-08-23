package sensors;

import utils.DeviceIdManager;
import utils.Log;

import java.time.Clock;
import java.util.*;

/**
 * 🧠 SensorFactory - Centralized creation, registration, and lookup for sensors.
 * Compatible with GUI and CLI, now auto-registering by default.
 */
public class SensorFactory {

    private static final Map<String, Sensor> registry = new HashMap<>();
    private static SensorCreator overrideCreator = null;

    // 🎯 Override hook for custom creators (useful for testing)
    public static void setSensorCreator(SensorCreator customCreator) {
        overrideCreator = customCreator;
    }

    public static void resetSensorCreator() {
        overrideCreator = null;
    }

    /**
     * 🔧 Unified sensor creation method
     */
    public static Sensor createSensor(SensorType type, String id, String name, MeasurementUnit unit, double currentValue, Clock clock) {
        Objects.requireNonNull(type, "SensorType cannot be null");
        Objects.requireNonNull(name, "Sensor name cannot be null");

        name = name.trim();

        // 🔠 Ensure ID generation is prefix-aware
        if (id == null || id.isBlank()) {
            String prefix = resolveSensorPrefix(type);
            id = DeviceIdManager.getInstance().generateIdWithPrefix(prefix);
        }

        // 🏗 Build Sensor using override or internal mapping
        Sensor sensor = (overrideCreator != null)
                ? overrideCreator.create(type, id, name, unit, currentValue, clock)
                : switch (type) {
            case LIGHT            -> new LightSensor(id, name, unit, currentValue, clock);
            case TEMPERATURE      -> new TemperatureSensor(id, name, unit, currentValue, clock);
//            case HUMIDITY         -> new HumiditySensor(id, name, unit, currentValue, clock);
            case MOTION           -> new MotionSensor(id, name, unit, currentValue, clock);
//            case SOFTENER_LEVEL   -> new SoftenerLevelSensor(id, name, unit, currentValue, clock);
//            case WATER_LEVEL,
//                    DETERGENT_LEVEL  -> new LiquidLevelSensor(id, name, unit, currentValue, clock);
            default               -> throw new IllegalArgumentException("🚫 Unsupported SensorType: " + type);
        };

        // 📥 Register into system memory
        registerSensor(sensor);
        return sensor;
    }

    /**
     * 🌐 Convenience method to create by type name (e.g. from Excel or GUI)
     */
    public static Sensor createSensorByType(String typeName, String id, String name, Clock clock) {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException("SensorType name cannot be empty.");
        }

        try {
            SensorType type = SensorType.valueOf(typeName.trim().toUpperCase());
            MeasurementUnit unit = MeasurementUnit.valueOf(type.getDefaultUnit().trim().toUpperCase());
            double defaultValue = type.getDefaultValue();
            Log.debug("🔍 SensorFactory - Creating sensor of type: " + type);
            return createSensor(type, id, name, unit, defaultValue, clock);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported sensor type: " + typeName);
        }
    }

    /**
     * 🔡 Resolves prefix used by DeviceIdManager for sensor IDs
     */
    private static String resolveSensorPrefix(SensorType type) {
        return switch (type) {
            case LIGHT            -> "LITs";
            case TEMPERATURE      -> "TMPs";
            case HUMIDITY         -> "HUMs";
            case MOTION           -> "MOTs";
            case SOFTENER_LEVEL   -> "WSLs";
            case DETERGENT_LEVEL,
                    WATER_LEVEL      -> "WDLs";
            default               -> type.name().substring(0, 3).toUpperCase() + "s";
        };
    }

    // 🗂️ Registry Access

    public static void registerSensor(Sensor sensor) {
        if (sensor == null || sensor.getSensorId() == null || sensor.getSensorId().isBlank()) {
            Log.warn("⚠️ Attempted to register invalid sensor.");
            return;
        }

        registry.put(sensor.getSensorId(), sensor);
        Log.debug("✅ Sensor registered: " + sensor.getSensorId());
    }

    public static boolean updateSensor(String id, String newName, double newValue, MeasurementUnit newUnit) {
        Sensor sensor = registry.get(id);
        if (sensor == null) {
            Log.warn("⚠️ SensorFactory.updateSensor - Sensor not found: " + id);
            return false;
        }

        if (newName != null && !newName.isBlank()) {
            sensor.setSensorName(newName.trim());
        }

        sensor.setCurrentValue(newValue);

        if (newUnit != null && newUnit != MeasurementUnit.UNKNOWN) {
            sensor.setUnit(newUnit);
        }

        Log.debug("✅ SensorFactory: Sensor '" + id + "' updated.");
        return true;
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

    public static void clearSensorById(String id) {
        if (id != null && registry.containsKey(id)) {
            registry.remove(id);
            Log.debug("🗑️ SensorFactory: Sensor '" + id + "' removed from registry.");
        }
    }
}
