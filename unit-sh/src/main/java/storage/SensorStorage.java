package storage;

import sensors.Sensor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SensorStorage {

    private static final Map<String, Sensor> sensors = new HashMap<>();

    // 🔄 Add or replace a sensor
    public static void addSensor(String sensorId, Sensor sensor) {
        sensors.put(sensorId, sensor);
    }

    // ❌ Remove a sensor
    public static Sensor removeSensor(String sensorId) {
        return sensors.remove(sensorId);
    }

    // 🔍 Get a specific sensor
    public static Sensor getSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    // 🗺️ Get all sensors (modifiable)
    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    // 📦 Get unmodifiable view (for safe display)
    public static Map<String, Sensor> getUnmodifiableSensors() {
        return Collections.unmodifiableMap(sensors);
    }

    // 🧹 Clear everything (e.g., for testing or reset)
    public static void clear() {
        sensors.clear();
    }
    public static void loadSensorsFromExcel() {
        // Delegate to XlCreator to load sensor data
        Map<String, Sensor> loaded = XlCreator.loadSensors();

        if (loaded != null) {
            sensors.clear();           // Clear existing map
            sensors.putAll(loaded);    // Populate with fresh Excel data
            System.out.println("✅ Loaded " + sensors.size() + " sensors from Excel.");
        } else {
            System.out.println("⚠️ No sensor data found in Excel.");
        }
    }
    public static void register(Sensor sensor) {
        if (sensor == null || sensor.getSensorId() == null) {
            System.err.println("❌ Invalid sensor or missing ID.");
            return;
        }
        sensors.put(sensor.getSensorId(), sensor);
    }
    public static boolean isNameTaken(String name) {
        if (name == null) return false;
        return sensors.values().stream()
                .anyMatch(sensor -> sensor.getSensorName().equalsIgnoreCase(name.trim()));
    }
    public static Collection<Sensor> getAll() {
        return sensors.values();
    }


}
