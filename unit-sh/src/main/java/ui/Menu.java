package ui;

import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import scheduler.Scheduler;
import storage.DeviceStorage;
import storage.XlCreator;
import utils.ClockUtil;
import utils.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Menu {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Clock clock = ClockUtil.getClock();
    private static final NotificationService notificationService = new NotificationService();

    public static void show(Map<String, Device> devices, List<Thread> deviceThreads, Scheduler scheduler) {
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
                case "2":
                    DeviceMonitor.showMonitorDeviceMenu(devices, scheduler);
                    break;

                case "3":
                    showScheduleMenu(devices, scheduler); // ✅ Now correctly passing `scheduler`
                    break;

                case "4":
                    // 🔍 Identify testable OFF devices
                    List<Device> offDevices = DeviceStorage.getDevices().values().stream()
                            .filter(device -> !device.isOn())
                            .toList();

                    if (offDevices.isEmpty()) {
                        System.out.println("📭 No devices available for testing. All are ON.");
                        break;
                    }

                    // 📋 Show available devices BEFORE asking for an ID
                    System.out.println("\n=== Testable Devices ===");
                    System.out.printf("%-8s%-20s%-8s%n", "ID", "NAME", "STATE");
                    System.out.println("--------------------------------");

                    offDevices.forEach(device -> System.out.printf(
                            "%-8s%-20s%-8s%n",
                            device.getId(),
                            device.getName(),
                            device.getState()
                    ));

                    // 👉 Ask user for an ID
                    System.out.print("Enter ID of the device to test (or 0 to cancel): ");
                    String testId = scanner.nextLine().trim();

                    if (testId.equals("0")) break; // 🚪 Exit if user cancels

                    // ✅ Fetch the selected device
                    Device selectedDevice = DeviceStorage.getDevices().get(testId);

                    if (selectedDevice == null || selectedDevice.isOn()) {
                        System.out.println("❌ Invalid choice or device is already ON.");
                    } else {
                        selectedDevice.testDevice();
                    }
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
                    // 🔎 Ensure state consistency before listing
                    DeviceStorage.getDevices().values().forEach(device -> {
                        System.out.println("🧐 Direct Boolean State: " + device.isOn()); // 🛠️ Verifying `isOn` directly
                    });

                    // 🧐 Check if storage is empty before displaying
                    if (DeviceStorage.getDevices().isEmpty()) {
                        System.out.println("📭 No devices found.");
                        return;
                    }

                    // 📋 Begin structured device listing
                    System.out.println("📋 Devices in System Memory:");
                    System.out.printf("%-16s%-20s%-8s%-9s%n", "  TYPE", "NAME", "   ID", " STATE");
                    System.out.println("-----------------------------------------------------");

                    // 🔎 Final debug before printing list

                    // ✅ Display formatted device list with final state confirmation
                    DeviceStorage.getDevices().values().forEach(device -> {
                        System.out.printf(
                                "%-2s%-16s%-20s%-8s%-8s%n",
                                "- ",
                                device.getType().name(),  // ✅ Explicitly use .name() to guarantee the correct DeviceType
                                device.getName(),
                                device.getId(),
                                device.getState()
                        );
                    });

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





    public static void showScheduleMenu(Map<String, Device> devices, Scheduler scheduler) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Scheduler Menu ===");
            System.out.println("1 - View    Tasks");
            System.out.println("2 - Set    a Task");
            System.out.println("3 - Update a Task");
            System.out.println("4 - Delete a Task");
            System.out.println("5 - Back");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> scheduler.printScheduledTasks();
                case "2" -> {
                    scheduleNewTask(devices, scheduler);
                    back = true;  // ✅ Return to main menu after scheduling
                }
                case "3" -> {
                    scheduler.printScheduledTasks();  // ✅ Now displays tasks before selecting
                    scheduler.updateTask(getTaskIndex(), getNewTaskTime(), getNewRepeat());
                }
                case "4" -> {
                    scheduler.removeTask(getTaskIndex());  // ✅ Calls method inside `Scheduler`
                    back = true;  // ✅ Return to main menu
                }
                case "5" -> back = true;
                default -> System.out.println("❌ Invalid option. Please choose 1-5.");
            }
        }
    }

    private static int getTaskIndex() {
        System.out.print("📌 Enter task number (or 0 to cancel): ");
        int taskIndex = Integer.parseInt(scanner.nextLine()) - 1;
        return (taskIndex < 0) ? -1 : taskIndex;
    }

    private static LocalDateTime getNewTaskTime() {
        System.out.print("🕒 Enter new scheduled time (yyyy-MM-dd HH:mm): ");
        return LocalDateTime.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private static String getNewRepeat() {
        System.out.print("🔁 Enter new repeat frequency (none, daily, weekly, monthly): ");
        return scanner.nextLine().trim().toLowerCase();
    }



    private static void scheduleNewTask(Map<String, Device> devices, Scheduler scheduler) {
        if (devices.isEmpty()) {
            System.out.println("📭 No available devices to schedule.");
            return;
        }

        // 📋 List available devices
        System.out.println("\n=== Schedule a Device ===");
        System.out.printf("%-8s%-20s%-10s%n", "  ID", "NAME", "TYPE");
        System.out.println("--------------------------------");

        devices.values().forEach(device -> System.out.printf(
                "%-8s%-20s%-10s%n",
                device.getId(),
                device.getName(),
                device.getType()
        ));

        // 🔍 Ask for device ID
        System.out.print("Enter ID of the device to schedule: ");
        String deviceId = scanner.nextLine().trim();

        Device selectedDevice = devices.get(deviceId);
        if (selectedDevice == null) {
            System.out.println("❌ Device not found.");
            return;
        }

        // 🔄 Ask for action
        System.out.print("Enter action to perform (e.g., 'on', 'off'): ");
        String action = scanner.nextLine().trim();

        // 🕒 Sub-menu for selecting scheduled time
        LocalDateTime scheduledTime = getScheduledTime();

        // 🔁 Ask for repeat frequency
        System.out.println("\nSelect Repeat Frequency:");
        System.out.println("1 - None");
        System.out.println("2 - Daily");
        System.out.println("3 - Monthly");
        System.out.print("Choose an option: ");
        String repeatChoice = scanner.nextLine().trim();
        String repeat = switch (repeatChoice) {
            case "2" -> "daily";
            case "3" -> "monthly";
            default -> "none";
        };

        // ✅ Schedule the task
        scheduler.scheduleTask(selectedDevice, action, scheduledTime, repeat);
        System.out.println("✅ Task scheduled successfully for " + selectedDevice.getName() + " at " + scheduledTime);
    }

    private static LocalDateTime getScheduledTime() {
        System.out.println("\nSelect Task Date:");
        System.out.println("1 - Set Task for Today");
        System.out.println("2 - Other Date");
        System.out.print("Choose an option: ");
        String dateChoice = scanner.nextLine().trim();

        LocalDate taskDate;
        if (dateChoice.equals("1")) {
            taskDate = LocalDate.now();
        } else {
            System.out.print("Enter Year: ");
            int year = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Day: ");
            int day = Integer.parseInt(scanner.nextLine().trim());

            taskDate = LocalDate.of(year, month, day);
        }

        System.out.print("Enter Time (HH:mm): ");
        String timeInput = scanner.nextLine().trim();
        LocalTime taskTime = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));

        return taskDate.atTime(taskTime);
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
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
