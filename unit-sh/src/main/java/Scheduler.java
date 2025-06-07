import org.apache.poi.ss.usermodel.*;
import scheduler.ScheduledTask;
import devices.Device;
import devices.GenericDevice;
import storage.TaskExcelStorage;
import utils.ClockUtil;

import java.io.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scheduler {

    private static final String FILE_PATH = "/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/scheduler.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
    private static final long CHECK_INTERVAL_MS = 60 * 1000;

    private final Map<String, Device> deviceRegistry = new HashMap<>();
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private final TaskExcelStorage excelStorage = new TaskExcelStorage();
    private Timer schedulerTimer;

    public Scheduler() {
        System.out.println("📅 Scheduler created (no tasks loaded yet)");
    }

    public void registerDevice(Device device) {
        String normalizedId = normalizeId(device.getId());
        deviceRegistry.put(normalizedId, device);
        System.out.println("🔌 Registered device: " + normalizedId + " (" + device.getName() + ")");
    }

    public void scheduleTask(Device device, String action, LocalDateTime time, String repeat) {
        device.setId(normalizeId(device.getId()));
        ScheduledTask task = new ScheduledTask(device, action, time, repeat);
        scheduledTasks.add(task);
        saveTasksToFile();
        System.out.println("✅ Task scheduled: " + task);
    }

    public void addTask(ScheduledTask task) {
        normalizeTaskDevice(task);
        scheduledTasks.add(task);
        saveTasksToFile();
    }

    public void editTask(int index, ScheduledTask updatedTask) {
        if (index >= 0 && index < scheduledTasks.size()) {
            normalizeTaskDevice(updatedTask);
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

                String id = normalizeId(parts[0]);
                String name = parts[1];
                String action = parts[2];
                LocalDateTime time = LocalDateTime.parse(parts[3], FORMATTER);
                String repeat = parts[4];

                Device device = deviceRegistry.get(id);
                if (device == null) {
                    System.out.println("⚠️ No registered device for ID " + id + " — using GenericDevice");
                    Clock clock = ClockUtil.getClock();
                    device = new GenericDevice(id, name, "Generic", clock);
                    deviceRegistry.put(id, device);
                }

                ScheduledTask task = new ScheduledTask(device, action, time, repeat);
                scheduledTasks.add(task);
            }

            System.out.println("✅ Loaded " + scheduledTasks.size() + " scheduled task(s).");

        } catch (IOException e) {
            System.out.println("❌ Failed to load tasks: " + e.getMessage());
        }
    }

    





    public void startSchedulerLoop() {
        schedulerTimer = new Timer(true);
        schedulerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndRunDueTasks();
            }
        }, 0, CHECK_INTERVAL_MS);

        System.out.println("🕒 Scheduler loop started (checking every " + (CHECK_INTERVAL_MS / 1000) + " seconds)");
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

    private void saveTasksToFile() {
        Map<String, List<ScheduledTask>> tasksByDeviceId = new HashMap<>();
        tasksByDeviceId.put("default", scheduledTasks);
        excelStorage.saveTasks(tasksByDeviceId);
    }

    private String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase();
    }

    private void normalizeTaskDevice(ScheduledTask task) {
        if (task.getDevice() != null) {
            String normalizedId = normalizeId(task.getDevice().getId());
            task.getDevice().setId(normalizedId);
            Device realDevice = deviceRegistry.get(normalizedId);
            if (realDevice != null) {
                task.setDevice(realDevice); // Prefer registered device object
            }
        }
    }

    public void loadTasksFromExcel(Map<String, List<ScheduledTask>> tasks) {
    }
}
