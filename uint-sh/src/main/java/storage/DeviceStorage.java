package storage;

import devices.*;
import utils.ClockUtil;
import utils.NotificationService;

import java.io.*;
import java.util.*;
import java.time.Clock;

public class DeviceStorage {

    private static final String FILE_NAME = "devices.txt";

    public static List<Device> loadDevices(NotificationService notificationService) {
        List<Device> loadedDevices = new ArrayList<>();
        Clock clock = ClockUtil.getClock(); // ✅ Shared clock instance

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 1) continue;

                String rawType = parts[0];
                String type = rawType.contains(".") ? rawType.substring(rawType.lastIndexOf('.') + 1) : rawType;

                switch (type) {
                    case "Light" -> loadedDevices.add(Light.fromDataString(parts, clock));
                    case "Thermostat" -> loadedDevices.add(Thermostat.fromDataString(parts, notificationService, clock));
                    case "WashingMachine" -> loadedDevices.add(WashingMachine.fromDataString(parts, clock));
                    case "Dryer" -> loadedDevices.add(Dryer.fromDataString(parts, clock));
                    default -> System.out.println("❓ Unknown device type: " + rawType);
                }
            }
        } catch (IOException e) {
            System.out.println("📭 No saved devices found or failed to read: " + e.getMessage());
        }

        return loadedDevices;
    }


    public static void saveDevices(List<Device> devices) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Device device : devices) {
                writer.write(device.toDataString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to save devices: " + e.getMessage());
        }
    }
}
