package ui;

import devices.*;
import storage.DeviceStorage;
import storage.XlCreator;

import java.time.Clock;
import java.util.*;
import devices.DeviceFactory;

public class AddDeviceMenu {

    private static Scanner scanner = new Scanner(System.in); // 🔓 Now mutable
    public static Clock clock = Clock.systemDefaultZone();    // ✅ Injected clock

    public static void setScanner(Scanner newScanner) {
        scanner = newScanner;
    }

    public static void addDeviceMenu(Map<String, Device> devices, List<Thread> deviceThreads) {
        System.out.println("\n=== Add a Device ===");

        // ✅ Filter out UNKNOWN from menu
        List<DeviceType> availableTypes = Arrays.stream(DeviceType.values())
                .filter(type -> type != DeviceType.UNKNOWN)
                .toList();

        for (int i = 0; i < availableTypes.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, capitalize(availableTypes.get(i).toString()));
        }

        System.out.println((availableTypes.size() + 1) + " - Back (to Device menu)");
        System.out.print("Select a device type (or " + (availableTypes.size() + 1) + " to cancel): ");

        String input = scanner.nextLine();

        try {
            int choice = Integer.parseInt(input);
            if (choice == availableTypes.size() + 1) return;

            if (choice < 1 || choice > availableTypes.size()) {
                System.out.println("❌ Invalid choice.");
                return;
            }

            DeviceType selectedType = availableTypes.get(choice - 1);
            System.out.print("Enter a name for the new " + capitalize(selectedType.toString()) + ": ");
            String name = scanner.nextLine().trim();

            boolean nameExists = devices.values().stream()
                    .anyMatch(device -> device.getName().equalsIgnoreCase(name));
            if (nameExists) {
                System.out.println("❌ A device with that name already exists. Please choose a different name.");
                return;
            }

            if (selectedType == DeviceType.SENSOR) {
                System.out.println("⚙️ Redirecting to Sensor creation menu...");
                AddSensorMenu.run(name);
                return;
            }

            Set<String> existingIds = new HashSet<>(DeviceStorage.getDevices().keySet());
            String id = utils.DeviceIdManager.getInstance().generateIdForType(selectedType);

            Device newDevice = DeviceFactory.createDevice(
                    selectedType, id, name, clock, DeviceFactory.getDevices()
            );

            System.out.print("Enter brand: ");
            String brand = scanner.nextLine().trim();

            System.out.print("Enter model: ");
            String model = scanner.nextLine().trim();

            newDevice.setBrand(brand);
            newDevice.setModel(model);

            devices.put(id, newDevice);
            Thread thread = new Thread(newDevice);
            thread.start();
            deviceThreads.add(thread);

            XlCreator.writeDeviceToExcel(newDevice);

            System.out.printf("✅ %s (%s) added successfully!%n", name, id);

        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("❌ Failed to add device: " + e.getMessage());
        }
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
