import devices.*;
import scheduler.ScheduledTask;
import storage.DeviceStorage;
import storage.TaskExcelStorage;
import utils.ClockUtil;
import utils.NotificationService;

import java.time.Clock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static storage.TaskExcelStorage.normalizeId;


public class SmartHomeSystem {



    private static final List<Thread> deviceThreads = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final NotificationService notificationService = new NotificationService();
    private static final Clock clock = ClockUtil.getClock();

    private static final Scheduler scheduler = new Scheduler();
    private static final Map<String, Device> devices =
            DeviceStorage.loadDevices(new NotificationService());


    public static void registerDevice(Device device) {
        devices.put(device.getId(), device);
    }

    public static Device getDeviceById(String id) {
        return devices.get(id);
    }

    public static Map<String, Device> getAllDevices() {

        return devices;
    }


    public static void main(String[] args) {

        log("📦 Loading devices...");
        NotificationService notificationService = new NotificationService();
        Map<String, Device> devices = DeviceStorage.loadDevices(notificationService);

        // ✅ Initialize light counters after loading devices
        Light.initializeLightCounter(devices);

        // ✅ Start all device threads first
        for (Device device : devices.values()) {
            Thread thread = new Thread(device);
            deviceThreads.add(thread);
            thread.start();
            log("🔌 Started device thread: " + device.getName() + " [" + device.getId() + "]");
        }

        // ✅ Now load scheduled tasks — devices should be registered by now
        log("📅 Loading scheduled tasks...");
        System.out.println("👀 Devices available before loading tasks:");
        devices.keySet().forEach(id -> System.out.println("  - " + id));
        Map<String, List<ScheduledTask>> tasks = TaskExcelStorage.loadTasks(devices);
        scheduler.loadTasksFromExcel(tasks);
        scheduler.startSchedulerLoop();

        // ✅ Confirmation
        log("🚀 Smart Home System started");

        // 🔁 Main menu loop
        boolean running = true;

        while (running) {
            System.out.println("\n=== Smart Home Main Menu ===");
            System.out.println("1 - Devices");
            System.out.println("2 - Monitor");
            System.out.println("3 - Schedule");
            System.out.println("4 - Test a Device");
            System.out.println("5 - Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> showDevicesMenu();
                case "2" -> System.out.println("[Monitor menu coming soon]");
                case "3" -> showScheduleMenu();
                case "4" -> toggleDevicePower();
                case "5" -> {
                    running = false;
                    DeviceStorage.saveDevices(devices);
                    log("💾 Devices saved before exit.");
                }
                default -> System.out.println("Invalid option. Please choose 1-5.");
            }
        }

        // Gracefully stop all threads
        for (Thread t : deviceThreads) {
            t.interrupt();
        }

        log("🛑 UNIT's Smart Home Simulation stopped.");
        scanner.close();
    }


    private static void log(String message) {
        System.out.println("[" + ClockUtil.getCurrentTimestamp() + "] " + message);
    }

    private static void showScheduleMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Scheduler Menu ===");
            System.out.println("1 - View Scheduled Tasks");
            System.out.println("2 - Schedule a Device");
            System.out.println("3 - Back");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> scheduler.printScheduledTasks();
                case "2" -> scheduleDevicePower();
                case "3" -> back = true;
                default -> System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }

    private static void scheduleDevicePower() {
        listDevices();
        if (devices.isEmpty()) return;

        System.out.print("Enter the ID of the device to schedule: ");
        String inputId = scanner.nextLine().trim();
        String normalizedId = normalizeId(inputId);  // 👈 normalize once, consistently

        Optional<Device> deviceOpt = devices.values().stream()
                .filter(d -> normalizeId(d.getId()).equals(normalizedId)) // match normalized
                .findFirst();

        if (deviceOpt.isEmpty()) {
            System.out.println("❌ No device found with that ID.");
            return;
        }

        Device device = deviceOpt.get();

        System.out.print("Enter time to execute (HH:mm, 24-hour): ");
        String timeInput = scanner.nextLine().trim();
        LocalTime time;
        try {
            time = LocalTime.parse(timeInput);
        } catch (DateTimeParseException e) {
            System.out.println("⚠️ Invalid time format. Use HH:mm format (e.g., 14:30).");
            return;
        }

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), time);

        System.out.print("Enter action (on/off): ");
        String action = scanner.nextLine().trim().toLowerCase();

        if (!action.equals("on") && !action.equals("off")) {
            System.out.println("⚠️ Invalid action. Only 'on' or 'off' allowed.");
            return;
        }

        scheduler.scheduleTask(device, action, dateTime, "none");

        // ✅ Now clearly shows device ID (normalized) in confirmation
        log("⏱️ Scheduled " + action.toUpperCase() + " for device " +
                device.getName() + " [" + normalizeId(device.getId()) + "] at " + time);
    }




    private static void showDevicesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== Devices Menu ===");
            System.out.println("1 - List");
            System.out.println("2 - Add");
            System.out.println("3 - Update");
            System.out.println("4 - Remove");
            System.out.println("5 - Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> listDevices();
                case "2" -> addDeviceInteractive();
                case "3" -> updateDeviceInteractive();
                case "4" -> removeDeviceInteractive();
                case "5" -> back = true;
                default -> System.out.println("Invalid option. Please choose 1-6.");
            }
        }
    }

    private static void listDevices() {
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


    private static void toggleDevicePower() {
        List<Device> testableDevices = devices.values().stream()
                .filter(d -> !d.isOn())
                .toList();

        if (testableDevices.isEmpty()) {
            System.out.println("⚠️ No devices available for testing (all are ON).");
            return;
        }

        System.out.println("\nDevices available for testing:");
        testableDevices.forEach(device ->
                System.out.println(device.getName() + " [" + device.getId() + "] | Status: OFF"));

        System.out.print("Enter device ID to test: ");
        String id = scanner.nextLine().trim();

        Optional<Device> target = testableDevices.stream()
                .filter(d -> d.getId().equalsIgnoreCase(id))
                .findFirst();

        if (target.isPresent()) {
            Device device = target.get();
            log("🧪 Starting test for device: " + device.getName() + " [" + device.getId() + "]");
            device.testDevice();
        } else {
            System.out.println("❌ No OFF device found with that ID. Make sure the device is OFF.");
        }
    }



    private static void addDevice(Device device) {
        devices.put(device.getId(), device);
        Thread thread = new Thread(device);
        deviceThreads.add(thread);
        thread.start();
        log("➕ Added and started new device: " + device.getName() + " [" + device.getId() + "]");
        DeviceStorage.saveDevices(devices);
    }

    private static boolean nameExists(String name) {
        return devices.values().stream()
                .anyMatch(device -> device.getName().equalsIgnoreCase(name));
    }

    private static void addDeviceInteractive() {
        System.out.println("Choose device type:");
        System.out.println("1 - Light");
        System.out.println("2 - Thermostat");
        System.out.println("3 - Washing Machine");
        System.out.println("4 - Dryer");
        System.out.print("Enter choice: ");
        String typeChoice = scanner.nextLine();

        System.out.print("Enter device name: ");
        String name = scanner.nextLine().trim();

        if (nameExists(name)) {
            System.out.println("❌ A device with this name already exists. Please choose a different name.");
            return;
        }

        switch (typeChoice) {
            case "1" -> addDevice(new Light(name, clock));
            case "2" -> {
                System.out.print("Enter min temperature: ");
                double minTemp = Double.parseDouble(scanner.nextLine());
                System.out.print("Enter max temperature: ");
                double maxTemp = Double.parseDouble(scanner.nextLine());
                addDevice(new Thermostat(name, minTemp, maxTemp, notificationService, clock));
            }
            case "3", "4" -> {
                System.out.print("Enter brand: ");
                String brand = scanner.nextLine().trim();
                System.out.print("Enter model: ");
                String model = scanner.nextLine().trim();

                if (typeChoice.equals("3")) {
                    addDevice(new WashingMachine(name, brand, model, clock));
                } else {
                    addDevice(new Dryer(name, brand, model, clock));
                }
            }
            default -> System.out.println("Invalid device type.");
        }
    }

    private static void removeDeviceInteractive() {
        listDevices();
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
            log("🗑️ Removed device: " + removed.getName() + " [" + removed.getId() + "]");
            DeviceStorage.saveDevices(devices);
        } else {
            System.out.println("❌ No device found with that ID.");
        }
    }

    private static void updateDeviceInteractive() {
        listDevices();
        if (devices.isEmpty()) return;

        System.out.print("Enter device ID to update: ");
        String input = scanner.nextLine().trim();

        Optional<Device> toUpdate = devices.values().stream()
                .filter(d -> d.getId().equalsIgnoreCase(input))
                .findFirst();

        if (toUpdate.isEmpty()) {
            System.out.println("❌ No device found with that ID.");
            return;
        }

        Device device = toUpdate.get();

        System.out.print("Enter new name for the device (or leave blank to keep '" + device.getName() + "'): ");
        String newName = scanner.nextLine().trim();

        if (!newName.isEmpty()) {
            boolean nameTaken = devices.values().stream()
                    .anyMatch(d -> !d.getId().equals(device.getId()) && d.getName().equalsIgnoreCase(newName));
            if (nameTaken) {
                System.out.println("❌ A device with that name already exists. Please choose another name.");
                return;
            }
            device.setName(newName);
        }

        if (device instanceof Thermostat thermostat) {
            System.out.print("Enter new temperature (current: " + thermostat.getTemperature() + "): ");
            String tempStr = scanner.nextLine().trim();
            if (!tempStr.isEmpty()) {
                try {
                    thermostat.setTemperature(Double.parseDouble(tempStr));
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid temperature input. Skipping.");
                }
            }

            System.out.print("Enter new threshold (current: " + thermostat.getThreshold() + "): ");
            String thresholdStr = scanner.nextLine().trim();
            if (!thresholdStr.isEmpty()) {
                try {
                    thermostat.setThreshold(Double.parseDouble(thresholdStr));
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid threshold input. Skipping.");
                }
            }
        }

        if (device instanceof Dryer dryer) {
            System.out.print("Enter new brand (current: " + dryer.getBrand() + "): ");
            String newBrand = scanner.nextLine().trim();
            if (!newBrand.isEmpty()) {
                dryer.setBrand(newBrand);
            }

            System.out.print("Enter new model (current: " + dryer.getModel() + "): ");
            String newModel = scanner.nextLine().trim();
            if (!newModel.isEmpty()) {
                dryer.setModel(newModel);
            }
        }

        log("✏️ devices.Device updated: " + device.getName() + " [" + device.getId() + "]");
        DeviceStorage.saveDevices(devices);
    }
}
