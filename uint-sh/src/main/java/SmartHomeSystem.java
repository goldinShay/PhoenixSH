import java.util.*;
import java.time.Clock; // ✅ Used for consistent time management
import java.time.ZonedDateTime;

public class SmartHomeSystem {

    private static final List<Device> devices = new ArrayList<>();
    private static final List<Thread> deviceThreads = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final NotificationService notificationService = new NotificationService();
    private static final Clock clock = ClockUtil.getClock(); // ✅ Shared Clock instance

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();

        log("📦 Loading devices...");
        devices.addAll(DeviceStorage.loadDevices(notificationService));

        for (Device device : devices) {
            Thread thread = new Thread(device);
            deviceThreads.add(thread);
            thread.start();
            log("🔌 Started device thread: " + device.getName() + " [" + device.getId() + "]");
        }

        boolean running = true;
        log("🚀 Smart Home System started");

        while (running) {
            System.out.println("\n=== Smart Home Main Menu ===");
            System.out.println("1 - Devices");
            System.out.println("2 - Monitor");
            System.out.println("3 - Schedule");
            System.out.println("4 - Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> showDevicesMenu();
                case "2" -> System.out.println("[Monitor menu coming soon]");
                case "3" -> System.out.println("[Schedule menu coming soon]");
                case "4" -> {
                    running = false;
                    DeviceStorage.saveDevices(devices); // Save on exit
                    log("💾 Devices saved before exit.");
                }
                default -> System.out.println("Invalid option. Please choose 1-4.");
            }
        }

        for (Thread t : deviceThreads) {
            t.interrupt();
        }

        log("🛑 Smart Home Simulation stopped.");
        scanner.close();
    }

    private static void log(String message) {
        System.out.println("[" + ClockUtil.getCurrentTimestamp() + "] " + message);
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
                default -> System.out.println("Invalid option. Please choose 1-5.");
            }
        }
    }

    private static void listDevices() {
        if (devices.isEmpty()) {
            System.out.println("No devices registered.");
        } else {
            System.out.println("\nRegistered Devices (sorted by ID):");
            devices.stream()
                    .sorted(Comparator.comparing(Device::getId))
                    .forEachOrdered(device ->
                            System.out.println(device.getName() + " [" + device.getId() + "]")
                    );
        }
    }

    // ✅ Uses Clock-aware constructor
    private static void addDevice(Device device) {
        devices.add(device);
        Thread thread = new Thread(device);
        deviceThreads.add(thread);
        thread.start();
        log("➕ Added and started new device: " + device.getName() + " [" + device.getId() + "]");
        DeviceStorage.saveDevices(devices);
    }

    private static boolean nameExists(String name) {
        return devices.stream()
                .anyMatch(device -> device.getName().equalsIgnoreCase(name));
    }


    private static void addDeviceInteractive() {
        System.out.println("Choose device type:");
        System.out.println("1 - Light");
        System.out.println("2 - Thermostat");
        System.out.println("3 - Washing Machine");
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
            case "3" -> {
                System.out.print("Enter brand: ");
                String brand = scanner.nextLine().trim();
                System.out.print("Enter model: ");
                String model = scanner.nextLine().trim();
                addDevice(new WashingMachine(name, brand, model, clock));
            }
            default -> System.out.println("Invalid device type.");
        }
    }



    private static void removeDeviceInteractive() {
        listDevices();
        if (devices.isEmpty()) return;

        System.out.print("Enter device ID to remove: ");
        String input = scanner.nextLine().trim();

        Optional<Device> toRemove = devices.stream()
                .filter(d -> d.getId().equalsIgnoreCase(input))
                .findFirst();

        if (toRemove.isPresent()) {
            Device removed = toRemove.get();

            // ✅ Mark as removed before deleting
            removed.markAsRemoved(clock);

            devices.remove(removed);
            log("🗑️ Removed device: " + removed.getName() + " [" + removed.getId() + "]");
            DeviceStorage.saveDevices(devices);
        } else {
            System.out.println("❌ No device found with that ID or name.");
        }
    }

    private static void updateDeviceInteractive() {
        listDevices();
        if (devices.isEmpty()) return;

        System.out.print("Enter device ID to update: ");
        String input = scanner.nextLine().trim();

        Optional<Device> toUpdate = devices.stream()
                .filter(d -> d.getId().equalsIgnoreCase(input))
                .findFirst();

        if (toUpdate.isEmpty()) {
            System.out.println("❌ No device found with that ID.");
            return;
        }

        Device device = toUpdate.get();

        // === Handle Name Update ===
        System.out.print("Enter new name for the device (or leave blank to keep '" + device.getName() + "'): ");
        String newName = scanner.nextLine().trim();

        if (!newName.isEmpty()) {
            boolean nameTaken = devices.stream()
                    .anyMatch(d -> !d.getId().equals(device.getId()) && d.getName().equalsIgnoreCase(newName));
            if (nameTaken) {
                System.out.println("❌ A device with that name already exists. Please choose another name.");
                return;
            }
            device.setName(newName);
        }

        // === Handle Thermostat-Specific Update ===
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

        log("✏️ Device updated: " + device.getName() + " [" + device.getId() + "]");
        DeviceStorage.saveDevices(devices);
    }

}
