package ui;

import devices.Device;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;

import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceViewer {

    public static Map<String, Object> getAllDevicesAndSensors() {
        Map<String, Object> combined = new LinkedHashMap<>();

        // Devices
        combined.putAll(DeviceStorage.getDevices());

        // Sensors
        SensorStorage.getUnmodifiableSensors().forEach(combined::put);

        return combined;
    }
    public static void displayAllDevicesAndSensors() {
        Map<String, Object> allItems = getAllDevicesAndSensors();

        if (allItems.isEmpty()) {
            System.out.println("📭 No devices or sensors found.");
            return;
        }

        System.out.println("📋 Devices and Sensors in System Memory:");
        System.out.printf("%-16s%-20s%-8s%-9s%n", "  TYPE", "NAME", "   ID", " STATE");
        System.out.println("-----------------------------------------------------");

        allItems.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 🔢 Sort by ID
                .forEach(entry -> {
                    String id = entry.getKey();
                    Object item = entry.getValue();
                    String type, name, state;

                    if (item instanceof Device device) {
                        type = device.getType().name();
                        name = device.getName();
                        state = device.getState();
                    } else if (item instanceof Sensor sensor) {
                        type = "SENSOR";
                        name = sensor.getSensorName();
                        state = sensor.getCurrentReading() + " " + sensor.getUnit();
                    } else {
                        type = "UNKNOWN";
                        name = "N/A";
                        state = "N/A";
                    }

                    System.out.printf("%-2s%-16s%-20s%-8s%-8s%n", "- ", type, name, id, state);
                });

    }

}
