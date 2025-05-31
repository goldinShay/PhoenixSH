import java.util.*;

public class SmartHomeSystem {

    private static final List<Device> devices = new ArrayList<>();
    private static final List<Thread> deviceThreads = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final NotificationService notificationService = new NotificationService();

    public static void main(String[] args) {

        Scheduler scheduler = new Scheduler();

        boolean running = true;
        while (running) {
            System.out.println("\n=== Smart Home Main Menu ===");
            System.out.println("1 - Devices");
            System.out.println("2 - Monitor");
            System.out.println("3 - Schedule");
            System.out.println("4 - Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showDevicesMenu();
                    break;
                case "2":
                    System.out.println("[Monitor menu coming soon]");
                    break;
                case "3":
                    System.out.println("[Schedule menu coming soon]");
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-4.");
            }
        }

        for (Thread t : deviceThreads) {
            t.interrupt();
        }

        System.out.println("Smart Home Simulation stopped.");
        scanner.close();
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
                case "1":
                    listDevices();
                    break;
                case "2":
                    addDeviceInteractive();
                    break;
                case "3":
                    System.out.println("[Update device coming soon]");
                    break;
                case "4":
                    removeDeviceInteractive();
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-5.");
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


    private static void addDevice(Device device) {
        devices.add(device);
        Thread thread = new Thread(device);
        deviceThreads.add(thread);
        thread.start();
    }

    private static void addDeviceInteractive() {
        System.out.println("Choose device type:");
        System.out.println("1 - Light");
        System.out.println("2 - Thermostat");
        System.out.print("Enter choice: ");
        String typeChoice = scanner.nextLine();

        System.out.print("Enter device name: ");
        String name = scanner.nextLine();

        switch (typeChoice) {
            case "1":
                addDevice(new Light(name));
                break;
            case "2":
                System.out.print("Enter min temperature: ");
                double minTemp = Double.parseDouble(scanner.nextLine());
                System.out.print("Enter max temperature: ");
                double maxTemp = Double.parseDouble(scanner.nextLine());
                addDevice(new Thermostat(name, minTemp, maxTemp, notificationService));
                break;
            default:
                System.out.println("Invalid device type.");
        }
    }

    private static void removeDeviceInteractive() {
        listDevices();
        if (devices.isEmpty()) return;

        System.out.print("Enter device number to remove: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index >= 0 && index < devices.size()) {
                Device removed = devices.remove(index);
                System.out.println("Removed device: " + removed.getName());
                // No direct thread shutdown here; just interrupt all and clean up later
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }
}
