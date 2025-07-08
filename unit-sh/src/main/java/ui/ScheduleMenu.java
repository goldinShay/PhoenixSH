package ui;

import devices.Device;
import scheduler.Scheduler;
import sensors.Sensor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduleMenu {

    public static void ScheduleMenu(Map<String, Device> devices, Scheduler scheduler, Scanner inputScanner) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Scheduler Menu ===");
            System.out.println("1 - View Tasks");
            System.out.println("2 - Set a Task");
            System.out.println("3 - Update a Task");
            System.out.println("4 - Delete a Task");
            System.out.println("5 - Back");
            System.out.print("Choose an option: ");
            String input = inputScanner.nextLine();

            switch (input) {
                case "1" -> scheduler.printScheduledTasks();
                case "2" -> scheduleNewTask(devices, scheduler, inputScanner);
                case "3" -> updateTaskFlow(scheduler, inputScanner);
                case "4" -> deleteTaskFlow(scheduler, inputScanner);
                case "5" -> back = true;
                default -> System.out.println("❌ Invalid option. Please choose 1-5.");
            }
        }
    }

    public static void ScheduleMenu(Map<String, Device> devices, Scheduler scheduler) {
        ScheduleMenu(devices, scheduler, new Scanner(System.in));
    }

    private static void scheduleNewTask(Map<String, Device> devices, Scheduler scheduler, Scanner scanner) {
        if (devices.isEmpty()) {
            System.out.println("📭 No available devices to schedule.");
            return;
        }

        System.out.println("\n=== Available Devices ===");
        System.out.printf("%-8s%-20s%-10s%n", "  ID", "NAME", "TYPE");
        System.out.println("--------------------------------");
        devices.values().forEach(device -> System.out.printf(
                "%-8s%-20s%-10s%n",
                device.getId(), device.getName(), device.getType()
        ));

        System.out.print("Enter ID of the device to schedule: ");
        String deviceId = scanner.nextLine().trim();
        Device selectedDevice = devices.get(deviceId);

        if (selectedDevice == null) {
            System.out.println("❌ Device not found.");
            return;
        }

        System.out.print("Enter action to perform (e.g., 'on', 'off'): ");
        String action = scanner.nextLine().trim();

        LocalDateTime scheduledTime = getScheduledTime(scanner);
        String repeat = getRepeatFrequency(scanner);

        scheduler.scheduleTask(selectedDevice, action, scheduledTime, repeat);
        System.out.println("✅ Task scheduled for " + selectedDevice.getName() + " at " + scheduledTime);
    }

    private static void updateTaskFlow(Scheduler scheduler, Scanner scanner) {
        scheduler.printScheduledTasks();
        int index = getTaskIndex(scanner);
        if (index >= 0) {
            LocalDateTime newTime = getNewTaskTime(scanner);
            String newRepeat = getNewRepeat(scanner);
            scheduler.updateTask(index, newTime, newRepeat);
        } else {
            System.out.println("❌ Update cancelled.");
        }
    }

    private static void deleteTaskFlow(Scheduler scheduler, Scanner scanner) {
        scheduler.printScheduledTasks();
        int index = getTaskIndex(scanner);
        if (index >= 0) {
            scheduler.removeTask(index);
            System.out.println("🗑️ Task removed.");
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    private static int getTaskIndex(Scanner scanner) {
        System.out.print("📌 Enter task number (or 0 to cancel): ");
        try {
            int taskIndex = Integer.parseInt(scanner.nextLine()) - 1;
            return (taskIndex < 0) ? -1 : taskIndex;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static LocalDateTime getScheduledTime(Scanner scanner) {
        System.out.println("\nSelect Task Date:");
        System.out.println("1 - Set Task for Today");
        System.out.println("2 - Choose Specific Date");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        LocalDate taskDate = choice.equals("1")
                ? LocalDate.now()
                : getCustomDate(scanner);

        System.out.print("Enter Time (HH:mm): ");
        String timeInput = scanner.nextLine().trim();
        LocalTime taskTime = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));

        return taskDate.atTime(taskTime);
    }

    private static LocalDate getCustomDate(Scanner scanner) {
        try {
            System.out.print("Enter Year: ");
            int year = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Day: ");
            int day = Integer.parseInt(scanner.nextLine().trim());

            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            System.out.println("❌ Invalid date input. Defaulting to today.");
            return LocalDate.now();
        }
    }

    private static LocalDateTime getNewTaskTime(Scanner scanner) {
        System.out.print("🕒 Enter new time (yyyy-MM-dd HH:mm): ");
        return LocalDateTime.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private static String getNewRepeat(Scanner scanner) {
        System.out.print("🔁 Enter new repeat (none, daily, weekly, monthly): ");
        return scanner.nextLine().trim().toLowerCase();
    }

    private static String getRepeatFrequency(Scanner scanner) {
        System.out.println("\nRepeat Task?");
        System.out.println("1 - None");
        System.out.println("2 - Daily");
        System.out.println("3 - Monthly");
        System.out.print("Choose an option: ");
        return switch (scanner.nextLine().trim()) {
            case "2" -> "daily";
            case "3" -> "monthly";
            default -> "none";
        };
    }
}
