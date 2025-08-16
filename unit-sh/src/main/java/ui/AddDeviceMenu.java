package ui;

import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import devices.actions.ApprovedDeviceModel;
import devices.actions.LiveDeviceState;
import storage.DeviceStorage;
import storage.xlc.nxl.DeviceWriteCoordinator;
import ui.gui.managers.GuiStateManager;

import java.time.Clock;
import java.util.*;


public class AddDeviceMenu {

    private static Scanner scanner = new Scanner(System.in); // 🔓 Now mutable
    public static Clock clock = Clock.systemDefaultZone();    // ✅ Injected clock

    public static void setScanner(Scanner newScanner) {
        scanner = newScanner;
    }

    public static void addDeviceMenu(Map<String, Device> devices, List<Thread> deviceThreads) {
        System.out.println("\n=== Add a Device ===");

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
                System.out.println("❌ A device with that name already exists.");
                return;
            }

            if (selectedType == DeviceType.SENSOR) {
                System.out.println("⚙️ Redirecting to Sensor creation menu...");
                AddSensorMenu.run(name);
                return;
            }

            Map<String, Device> storedDevices = DeviceStorage.getDevices();
            if (!storedDevices.isEmpty()) {
                utils.DeviceIdManager.getInstance().setExistingDevices(new ArrayList<>(storedDevices.values()));
            }

            System.out.println("📋 Known IDs before generation: " + devices.keySet());
            String id = utils.DeviceIdManager.getInstance().generateIdForType(selectedType);

            // 🌟 Select brand/model *before* creating device
            ApprovedDeviceModel approved = null;
            System.out.println("🔍 Choose a brand/model (or press Enter to skip):");
            List<ApprovedDeviceModel> matches = ApprovedDeviceModel.getByType(selectedType);

            for (int i = 0; i < matches.size(); i++) {
                ApprovedDeviceModel entry = matches.get(i);
                System.out.printf("%d - %s %s%n", i + 1, entry.getBrand(), entry.getModel());
            }

            System.out.print("Your choice: ");
            String brandModelChoice = scanner.nextLine().trim();

            if (!brandModelChoice.isEmpty()) {
                try {
                    int bmIndex = Integer.parseInt(brandModelChoice);
                    if (bmIndex >= 1 && bmIndex <= matches.size()) {
                        approved = matches.get(bmIndex - 1);
                        System.out.println("🟢 Selected: " + approved.getBrand() + " / " + approved.getModel());
                    } else {
                        System.out.println("❌ Invalid index.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid input.");
                }
            } else {
                System.out.println("ℹ️ No selection — defaulting to Unknown brand/model.");
            }

            String brand = (approved != null && !approved.getBrand().isBlank()) ? approved.getBrand() : "Unknown";
            String model = (approved != null && !approved.getModel().isBlank()) ? approved.getModel() : "Unknown";

            Device newDevice = DeviceFactory.createDevice(
                    selectedType, id, name, clock, DeviceFactory.getDevices(), approved, brand, model
            );
            newDevice.setBrand(brand);
            newDevice.setModel(model);

            devices.put(id, newDevice);
            Thread thread = new Thread(newDevice);
            thread.start();
            deviceThreads.add(thread);

            DeviceWriteCoordinator.writeDeviceToWorkbook(newDevice);

            System.out.printf("✅ %s (%s) added successfully!%n", name, id);

            // 🧠 GUI sync block — make it visible in the matrix
            GuiStateManager.registerNewDevice(newDevice);
            LiveDeviceState.turnOn(newDevice); // or turnOff if preferred
            DeviceStorage.getDevices().put(newDevice.getId(), newDevice); // safe redundancy
            GuiStateManager.refreshDeviceMatrix();
            System.out.println("✅ " + newDevice.getName() + " (" + newDevice.getId() + ") added to GUI button map successfully!");

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
