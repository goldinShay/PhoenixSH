package ui;

import devices.*;
import storage.DeviceStorage;
import utils.ClockUtil;
import utils.NotificationService;
import utils.DeviceIdManager;
import storage.XlCreator;
import java.util.Map;
import java.util.List;
import devices.Device;

import java.time.Clock;
import java.util.*;

public class Menu {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Clock clock = ClockUtil.getClock();
    private static final NotificationService notificationService = new NotificationService();

    public static void show(Map<String, Device> devices, List<Thread> deviceThreads) {

        while (true) {
            System.out.println("\n=== WELCOME TO PhoenixSH ===");
            System.out.println("1. Device Menu");
            System.out.println("2. Monitor Device");
            System.out.println("3. Scheduler");
            System.out.println("4. Test Device");
            System.out.println("5. Exit");
            System.out.print("Please Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showDevicesMenu(devices, deviceThreads);
                    break;
                case "4":
                    testDevice();
                    break;
                case "5":
                    System.out.println("👋 Exiting Smart Home System. Goodbye!");
                    return;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        }
    }

    public static void showDevicesMenu(Map<String, Device> devices, List<Thread> deviceThreads)
    {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Devices Menu ===");
            System.out.println("1 - List Devices");
            System.out.println("2 - Add Device");
            System.out.println("3 - Update Device");
            System.out.println("4 - Remove Device");
            System.out.println("5 - Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.println("🛠️ Debug - Devices in Storage: " + DeviceStorage.getDevices().size());

                    if (DeviceStorage.getDevices().isEmpty()) {
                        System.out.println("📭 No devices found.");
                    } else {
                        System.out.println("📋 Devices in System Memory:");
                        System.out.printf("%-16s%-20s%-8s%-9s%n", "  TYPE", "NAME", "   ID", " STATE");
                        System.out.println("-----------------------------------------------------");

                        DeviceStorage.getDevices().values().forEach(device -> System.out.printf(
                                "%-2s%-16s%-20s%-8s%-8s%n",
                                "- ",
                                device.getType(),
                                device.getName(),
                                device.getId(),
                                device.getState()
                        ));
                    }
                }



                case "2" -> addDeviceSubMenu(devices, deviceThreads);

                case "3" -> {
                    System.out.print("Enter ID of the device to update: ");
                    String updateId = scanner.nextLine().trim();

                    // ✅ Verify device exists in DeviceStorage
                    Device device = DeviceStorage.getDevices().get(updateId);
                    if (device == null) {
                        System.out.println("❌ Device not found.");
                        return;
                    }

                    // 👉 Ask for new details
                    System.out.print("Enter new name (current: " + device.getName() + "): ");
                    String newName = scanner.nextLine().trim();

                    System.out.print("Enter new brand (current: " + device.getBrand() + "): ");
                    String newBrand = scanner.nextLine().trim();

                    System.out.print("Enter new model (current: " + device.getModel() + "): ");
                    String newModel = scanner.nextLine().trim();

                    // 🔄 Apply updates
                    device.setName(newName.isEmpty() ? device.getName() : newName);
                    device.setBrand(newBrand.isEmpty() ? device.getBrand() : newBrand);
                    device.setModel(newModel.isEmpty() ? device.getModel() : newModel);

                    // 🌟 Update timestamp
                    device.updateTimestamp();

                    // 💾 Persist changes to Excel
                    boolean updated = XlCreator.updateDevice(device);
                    if (updated) {
                        System.out.println("✅ Device updated successfully!");
                    } else {
                        System.out.println("❌ Failed to update device.");
                    }
                }



                case "4" -> {
                    System.out.print("Enter ID of the device to remove: ");
                    String removeId = scanner.nextLine().trim();

                    boolean removed = XlCreator.removeDevice(removeId);
                    if (removed) {
                        System.out.println("🗑️ Device removed.");
                    } else {
                        System.out.println("❌ Device not found.");
                    }
                }

                case "5" -> back = true;

                default -> System.out.println("❌ Invalid option. Please choose 1-5.");
            }
        }
    }

    private static void addDeviceSubMenu(Map<String, Device> devices, List<Thread> deviceThreads) {
        System.out.println("\n=== Add a Device ===");

        // ✅ Filter out GENERIC from the menu options
        List<DeviceType> availableTypes = Arrays.stream(DeviceType.values())
                .filter(type -> type != DeviceType.GENERIC)
                .toList();

        for (int i = 0; i < availableTypes.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, capitalize(availableTypes.get(i).toString()));
        }

        // 🔙 Add the "Back" option
        System.out.println("5 - Back (to Device menu)");

        System.out.print("Select a device type (or 5 to cancel): ");
        String input = scanner.nextLine();

        try {
            int choice = Integer.parseInt(input);
            if (choice == 5) return;  // ✅ Handles Back option correctly
            if (choice < 1 || choice > availableTypes.size()) {
                System.out.println("❌ Invalid choice.");
                return;
            }

            DeviceType selectedType = availableTypes.get(choice - 1); // ✅ Uses filtered list

            // 👉 Ask for device name
            System.out.print("Enter a name for the new " + capitalize(selectedType.toString()) + ": ");
            String name = scanner.nextLine().trim();

            // 🚫 Check for duplicate name
            boolean nameExists = devices.values().stream()
                    .anyMatch(device -> device.getName().equalsIgnoreCase(name));
            if (nameExists) {
                System.out.println("❌ A device with that name already exists. Please choose a different name.");
                return;
            }

            // ✅ Generate unique ID using DeviceIdManager
            Set<String> existingIds = new HashSet<>(DeviceStorage.getDevices().keySet());
            String id = XlCreator.getNextAvailableId(selectedType.toString().substring(0, 2), existingIds); // 🔥 Ensure unique ID generation

            // ⚙️ Instantiate the new device using the factory
            Map<String, Device> allDevicesMap = DeviceFactory.getDevices();
            Device newDevice = DeviceFactory.createDevice(
                    selectedType,
                    id,
                    name,
                    clock,
                    allDevicesMap
            );

            // 💾 Add device to map and start its thread
            devices.put(id, newDevice);
            Thread thread = new Thread(newDevice);
            thread.start();
            deviceThreads.add(thread);

            // 📄 Persist to Excel
            XlCreator.writeDeviceToExcel(newDevice);

            System.out.printf("✅ %s (%s) added successfully!%n", name, id);

        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("❌ Failed to add device: " + e.getMessage());
        }
    }





    public static void showScheduleMenu(Map<String, Device> devices) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Scheduler Menu ===");
            System.out.println("1 - View Scheduled Tasks");
            System.out.println("2 - Schedule a Device");
            System.out.println("3 - Back");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> System.out.println("Soon");
                case "2" -> System.out.println("Soon");
                case "3" -> back = true;
                default -> System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }

    private static void listDevices(Map<String, Device> devices) {
        if (devices.isEmpty()) {
            System.out.println("No devices registered.");
        } else {
            System.out.println("\nRegistered Devices (sorted by ID):");
            devices.values().stream()
                    .sorted(Comparator.comparing(Device::getId))
                    .forEachOrdered(device ->
                            System.out.println(device.getName() + " [" + device.getId() + "] | Status: " + (device.isOn() ? "ON" : "OFF"))
                    );
        }
    }

    private static boolean nameExists(Map<String, Device> devices, String name) {
        return devices.values().stream()
                .anyMatch(device -> device.getName().equalsIgnoreCase(name));
    }

    private static void removeDeviceInteractive(Map<String, Device> devices) {
        listDevices(devices);
        if (devices.isEmpty()) return;

        System.out.print("Enter device ID to remove: ");
        String input = scanner.nextLine().trim();

        Optional<Device> toRemove = devices.values().stream()
                .filter(d -> d.getId().equalsIgnoreCase(input))
                .findFirst();

        if (toRemove.isPresent()) {
            Device removed = toRemove.get();
            removed.markAsRemoved();
            devices.remove(removed);
            System.out.println("🗑️ Removed device: " + removed.getName() + " [" + removed.getId() + "]");
            DeviceStorage.removeDevice(removed.getId());  // ✅ Uses built-in removal method

        } else {
            System.out.println("❌ No device found with that ID.");
        }
    }

    private static void testDevice() {
        // 🔍 Filter out devices that are OFF
        List<Device> offDevices = DeviceStorage.getDevices().values().stream()
                .filter(device -> !device.isOn())
                .toList();

        if (offDevices.isEmpty()) {
            System.out.println("📭 No devices available for testing. All are ON.");
            return;
        }

        // 📋 List available devices for testing
        System.out.println("\n=== Test Device ===");
        System.out.printf("%-8s%-20s%-8s%n", "  ID", "NAME", "STATE");
        System.out.println("--------------------------------");

        offDevices.forEach(device -> System.out.printf(
                "%-8s%-20s%-8s%n",
                device.getId(),
                device.getName(),
                "Off"
        ));

        // 👉 Ask user to select a device by ID
        System.out.print("Enter ID of the device to test (or 0 to cancel): ");
        String testId = scanner.nextLine().trim();

        if (testId.equals("0")) return;

        // ✅ Find the selected device
        Device deviceToTest = DeviceStorage.getDevices().get(testId);
        if (deviceToTest == null || deviceToTest.isOn()) {
            System.out.println("❌ Invalid choice or device is already ON.");
            return;
        }

        // 🔄 Start test sequence
        System.out.println("🧪 Testing device: " + deviceToTest.getName());

        new Thread(() -> {
            try {
                deviceToTest.turnOn();
                System.out.println("🟢 " + deviceToTest.getName() + " is ON for testing...");
                Thread.sleep(10_000);
                deviceToTest.turnOff();
                System.out.println("🔴 " + deviceToTest.getName() + " has returned to OFF state.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("⚠️ Test interrupted for " + deviceToTest.getName());
            }
        }).start();
    }


    private static void handleAddDevice(DeviceType selectedType, Map<String, Device> devices, List<Thread> deviceThreads) {
        // You can move or merge this logic from addDeviceInteractive if needed
        // Currently not used in the original paste, but declared
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
