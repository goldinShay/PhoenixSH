
import devices.Device;
import devices.GenericDevice;
import utils.ClockUtil;

import java.io.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scheduler {

    private static final String FILE_PATH = "/home/nira/Documents/Shay/Fleur/unit-sh/uint-sh/scheduler.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    private final Map<String, Device> deviceRegistry = new HashMap<>();
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private Timer schedulerTimer;

    // 👇 Removed task loading from constructor!
    public Scheduler() {
        System.out.println("📅 Scheduler created (no tasks loaded yet)");
    }

    // 👇 Call this AFTER device registration
    public void loadTasksAfterDeviceRegistration() {
        System.out.println("📂 Loading scheduled tasks from file...");

        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("📭 No task file found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 5) continue;

                String id = parts[0];
                String name = parts[1];
                String action = parts[2];
                LocalDateTime time = LocalDateTime.parse(parts[3], FORMATTER);
                String repeat = parts[4];

                Device realDevice = deviceRegistry.get(id);
                if (realDevice == null) {
                    System.out.println("⚠️ No registered device for ID " + id + " — using GenericDevice");
                    Clock clock = ClockUtil.getClock();
                    realDevice = new GenericDevice(id, name, "Generic", clock);
                }

                ScheduledTask task = new ScheduledTask(realDevice, action, time, repeat);
                scheduledTasks.add(task);
            }

            System.out.println("✅ Loaded " + scheduledTasks.size() + " scheduled task(s).");

        } catch (IOException e) {
            System.out.println("❌ Failed to load tasks: " + e.getMessage());
        }
    }

    public void registerDevice(Device device) {
        deviceRegistry.put(device.getId(), device);
        System.out.println("🔌 Registered device: " + device.getId() + " (" + device.getName() + ")");
    }

    public void scheduleTask(Device device, String action, LocalDateTime time, String repeat) {
        ScheduledTask task = new ScheduledTask(device, action, time, repeat);
        scheduledTasks.add(task);
        saveTasksToFile();
        System.out.println("✅ Task scheduled: " + task);
    }

    public void addTask(ScheduledTask task) {
        scheduledTasks.add(task);
        saveTasksToFile();
    }

    public void editTask(int index, ScheduledTask updatedTask) {
        if (index >= 0 && index < scheduledTasks.size()) {
            scheduledTasks.set(index, updatedTask);
            saveTasksToFile();
        }
    }

    public void removeTask(int index) {
        if (index >= 0 && index < scheduledTasks.size()) {
            scheduledTasks.remove(index);
            saveTasksToFile();
        }
    }

    public List<ScheduledTask> getAllTasks() {
        return scheduledTasks;
    }

    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (ScheduledTask task : scheduledTasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to save tasks: " + e.getMessage());
        }
    }

    public void printScheduledTasks() {
        if (scheduledTasks.isEmpty()) {
            System.out.println("📭 No scheduled tasks.");
            return;
        }

        System.out.println("\n🗓️ Scheduled Tasks:");
        int index = 1;
        for (ScheduledTask task : scheduledTasks) {
            System.out.println(index++ + ". " + task);
        }
    }

    public void startSchedulerLoop() {
        schedulerTimer = new Timer(true); // Daemon thread
        schedulerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndRunDueTasks();
            }
        }, 0, 60 * 1000); // Every 60 seconds

        System.out.println("🕒 Scheduler loop started (checking every minute)");
    }

    private void checkAndRunDueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTask> dueTasks = new ArrayList<>();

        for (ScheduledTask task : scheduledTasks) {
            if (!task.getTime().isAfter(now)) {
                dueTasks.add(task);
            }
        }

        for (ScheduledTask task : dueTasks) {
            System.out.println("⏰ Running task: " + task);
            task.getDevice().performAction(task.getAction());
            rescheduleTask(task);
        }

        saveTasksToFile();
    }

    private void rescheduleTask(ScheduledTask task) {
        String repeat = task.getRepeat() != null ? task.getRepeat().toLowerCase() : "none";

        switch (repeat) {
            case "daily":
                task.setTime(task.getTime().plusDays(1));
                break;
            case "weekly":
                task.setTime(task.getTime().plusWeeks(1));
                break;
            case "monthly":
                task.setTime(task.getTime().plusMonths(1));
                break;
            case "none":
            case "":
                scheduledTasks.remove(task);
                break;
            default:
                System.out.println("⚠️ Unknown repeat value: " + repeat);
        }
    }

    // Inner class remains the same
    public static class ScheduledTask {
        private Device device;
        private String action;
        private LocalDateTime time;
        private String repeat;

        public ScheduledTask(Device device, String action, LocalDateTime time, String repeat) {
            this.device = device;
            this.action = action;
            this.time = time;
            this.repeat = repeat;
        }

        public Device getDevice() {
            return device;
        }

        public String getAction() {
            return action;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public String getRepeat() {
            return repeat;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        public void setRepeat(String repeat) {
            this.repeat = repeat;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        @Override
        public String toString() {
            return "[" + time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] "
                    + device.getName() + " - " + action + " (Repeat: " + repeat + ")";
        }

        public String toFileString() {
            return device.getId() + "|" + device.getName() + "|" + action + "|" + time.format(FORMATTER) + "|" + repeat;
        }
    }
}
