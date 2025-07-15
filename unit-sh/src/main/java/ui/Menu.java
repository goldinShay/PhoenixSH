package ui;

import devices.Device;
import scheduler.Scheduler;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import utils.ClockUtil;
import utils.NotificationService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Menu {

    static final Scanner scanner = new Scanner(System.in);
    private static final Clock clock = ClockUtil.getClock();
    private static final NotificationService notificationService = new NotificationService();

    public static void show(Map<String, Device> devices, List<Thread> deviceThreads, Scheduler scheduler) {
        show(devices, deviceThreads, scheduler, scanner); // 👈 default to real scanner
    }

    public static void show(Map<String, Device> devices, List<Thread> deviceThreads, Scheduler scheduler, Scanner inputScanner) {
        while (true) {
            System.out.println("\n=== WELCOME TO PhoenixSH ===");
            System.out.println("1. Device Menu");
            System.out.println("2. Monitor Device");
            System.out.println("3. Scheduler");
            System.out.println("4. Test Device");
            System.out.println("5. Exit");
            System.out.print("Please Select an option: ");

            String choice = inputScanner.nextLine().trim();

            switch (choice) {
                case "1" -> DeviceMenu.DevicesMenu(devices, deviceThreads);
                case "2" -> DeviceMonitor.showMonitorDeviceMenu(devices, scheduler);
                case "3" -> ScheduleMenu.ScheduleMenu(devices, scheduler);

                case "4" -> {
                    List<Device> offDevices = DeviceStorage.getDevices().values().stream()
                            .filter(device -> !device.isOn())
                            .toList();

                    List<Sensor> sensors = new ArrayList<>(SensorStorage.getSensors().values());

                    if (offDevices.isEmpty() && sensors.isEmpty()) {
                        System.out.println("📭 No devices or sensors available for testing.");
                        break;
                    }

                    System.out.println("\n=== Testable Devices & Sensors ===");
                    System.out.printf("%-10s%-8s%-20s%-8s%n", "TYPE", "ID", "NAME", "STATE");
                    System.out.println("-----------------------------------------------");

                    offDevices.forEach(device -> System.out.printf(
                            "%-10s%-8s%-20s%-8s%n",
                            "Device", device.getId(), device.getName(), device.getState()
                    ));

                    sensors.forEach(sensor -> System.out.printf(
                            "%-10s%-8s%-20s%-8s%n",
                            "Sensor", sensor.getSensorId(), sensor.getSensorName(),
                            sensor.getCurrentReading() + " " + sensor.getUnit()
                    ));

                    System.out.print("Enter ID of the device or sensor to test (or 0 to cancel): ");
                    String testId = inputScanner.nextLine().trim();

                    if (testId.equals("0")) break;

                    if (DeviceStorage.getDevices().containsKey(testId)) {
                        Device selected = DeviceStorage.getDevices().get(testId);
                        if (selected.isOn()) {
                            System.out.println("❌ Device is already ON. Turn it off before testing.");
                        } else {
                            selected.testDevice();
                        }
                    } else if (SensorStorage.getSensors().containsKey(testId)) {
                        Sensor selectedSensor = SensorStorage.getSensors().get(testId);
                        selectedSensor.testSensorBehavior();
                    } else {
                        System.out.println("❌ No device or sensor found with ID: " + testId);
                    }
                }

                case "5" -> {
                    System.out.println("👋 Exiting Smart Home System. Goodbye!");
                    return;
                }

                default -> System.out.println("❌ Invalid option. Please try again.");
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
        // To be implemented
    }

    static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
