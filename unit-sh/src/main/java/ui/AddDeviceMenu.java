package ui;

import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import storage.DeviceStorage;
import storage.XlCreator;

import java.time.Clock;
import java.util.*;

import static ui.Menu.capitalize;
import static ui.Menu.scanner;

public class AddDeviceMenu {

    public static Clock clock = Clock.systemDefaultZone();

    public static void addDeviceMenu(Map<String, Device> devices, List<Thread> deviceThreads) {
        System.out.println("\n=== Add a Device ===");

        // ✅ Filter out GENERIC from menu
        List<DeviceType> availableTypes = Arrays.stream(DeviceType.values())
                .filter(type -> type != DeviceType.GENERIC)
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

            // 👉 Ask for device name
            System.out.print("Enter a name for the new " + capitalize(selectedType.toString()) + ": ");
            String name = scanner.nextLine().trim();

            // 🚫 Check duplicate name
            boolean nameExists = devices.values().stream()
                    .anyMatch(device -> device.getName().equalsIgnoreCase(name));
            if (nameExists) {
                System.out.println("❌ A device with that name already exists. Please choose a different name.");
                return;
            }

            // 🧠 SENSOR gets a different path now
            if (selectedType == DeviceType.SENSOR) {
                System.out.println("⚙️ Redirecting to Sensor creation menu...");
                AddSensorMenu.run(name); // 🔮 You’ll implement this method/class next
                return;
            }

            // ✅ Proceed with standard device creation
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
}
