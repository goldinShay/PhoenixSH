import java.io.*;
import java.util.*;

public class DeviceStorage {
    private static final String FILE_NAME = "devices.txt";
    private static final String FILE_PATH = "/home/nira/Documents/Shay/Fleur/unit-sh/DeviceList.txt";


    public static void saveDevices(List<Device> devices) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Device device : devices) {
                writer.println(device.toDataString());
            }
        } catch (IOException e) {
            System.out.println("Failed to save devices: " + e.getMessage());
        }
    }


    public static List<Device> loadDevices(NotificationService notificationService) {
        List<Device> loadedDevices = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return loadedDevices;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String type = parts[0];
                Device device = null;

                switch (type) {
                    case "Light" -> device = Light.fromDataString(parts);
                    case "Thermostat" -> device = Thermostat.fromDataString(parts, notificationService);
                    // Add new device types here, one-liner style
                }

                if (device != null) {
                    loadedDevices.add(device);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load devices: " + e.getMessage());
        }
        return loadedDevices;
    }

}
