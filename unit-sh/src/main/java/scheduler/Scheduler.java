package scheduler;

import devices.Device;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DeviceStorage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scheduler {

    private static final String EXCEL_FILE = "/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx";
    private static final String TASKS_SHEET = "Scheduled Tasks";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final long CHECK_INTERVAL_MS = 30 * 1000;

    private final Map<String, Device> deviceRegistry;
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private Timer schedulerTimer;

    // 🔹 **Single Constructor: Guarantees deviceRegistry is initialized properly**
    public Scheduler(Map<String, Device> deviceRegistry) {
        this.deviceRegistry = (deviceRegistry != null) ? deviceRegistry : new HashMap<>();
        System.out.println("📅 Scheduler initialized with access to devices.");
    }

    // 🔹 Registers a new device into the Scheduler
    public void registerDevice(Device device) {
        deviceRegistry.put(device.getId(), device);
        System.out.println("🔌 Registered device: " + device.getId() + " (" + device.getName() + ")");
    }

    // 🔹 Schedules a new task and saves it to Excel
    public void scheduleTask(Device device, String action, LocalDateTime time, String repeat) {
        ScheduledTask task = new ScheduledTask(device, action, time, repeat);
        scheduledTasks.add(task);
        saveTasksToExcel();
        System.out.println("✅ Task scheduled: " + task);
    }

    // 🔹 Removes a task and updates the Excel file
    public void removeTask(int index) {
        if (index >= 0 && index < scheduledTasks.size()) {
            scheduledTasks.remove(index);
            saveTasksToExcel();
            System.out.println("🗑️ Task removed.");
        }
    }

    // 🔹 Displays all scheduled tasks
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

    // 🔹 Starts the automatic scheduler loop to check due tasks
    public void startSchedulerLoop() {
        schedulerTimer = new Timer(true);
        schedulerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndRunDueTasks();
            }
        }, 0, CHECK_INTERVAL_MS);

        System.out.println("🕒 Scheduler loop started (checking every " + (CHECK_INTERVAL_MS / 1000) + " seconds).");
    }

    // 🔹 Runs tasks that are due
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
            DeviceStorage.updateDeviceState(task.getDevice().getId(), task.getAction()); // ✅ Sync state after executionScheduled tasks saved to Excel without deleting other sheets.
            rescheduleTask(task);
        }

        saveTasksToExcel();
    }

    // 🔹 Reschedules recurring tasks
    private void rescheduleTask(ScheduledTask task) {
        switch (task.getRepeat().toLowerCase()) {
            case "daily" -> task.setTime(task.getTime().plusDays(1));
            case "weekly" -> task.setTime(task.getTime().plusWeeks(1));
            case "monthly" -> task.setTime(task.getTime().plusMonths(1));
            case "none" -> scheduledTasks.remove(task);
            default -> System.out.println("⚠️ Unknown repeat value: " + task.getRepeat());
        }
    }

    // 🔹 Saves tasks to Excel without wiping other sheets
    private void saveTasksToExcel() {
        File file = new File(EXCEL_FILE);
        Workbook workbook;

        try (FileInputStream fis = file.exists() ? new FileInputStream(file) : null) {
            workbook = fis != null ? new XSSFWorkbook(fis) : new XSSFWorkbook();
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Failed to read existing Excel file. Creating a new workbook.");
            workbook = new XSSFWorkbook();
        }

        Sheet sheet = workbook.getSheet(TASKS_SHEET);
        if (sheet == null) sheet = workbook.createSheet(TASKS_SHEET);

        String[] headers = {"DEVICE ID", "DEVICE NAME", "ACTION", "SCHEDULED", "REPEAT"};
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
        }

        int rowIndex = 1; // ✅ Always starts from row 1 to avoid repeated headers
        for (ScheduledTask task : scheduledTasks) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(task.getDevice().getId());
            row.createCell(1).setCellValue(task.getDevice().getName());
            row.createCell(2).setCellValue(task.getAction());
            row.createCell(3).setCellValue(task.getTime().format(FORMATTER));
            row.createCell(4).setCellValue(task.getRepeat());
        }

        try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE)) {
            workbook.write(fos);
//            System.out.println("✅ Scheduled tasks saved to Excel without deleting other sheets.");
        } catch (IOException e) {
            System.err.println("❌ Failed to save scheduled tasks: " + e.getMessage());
        }
    }

    // 🔹 Loads tasks from Excel and correctly links them to registered devices
    public void loadTasksFromExcel() {
        scheduledTasks.clear();

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(EXCEL_FILE))) {
            Sheet sheet = workbook.getSheet(TASKS_SHEET);
            if (sheet == null) {
                System.out.println("📭 No task sheet found.");
                return;
            }

            System.out.println("📂 Debug - Devices available when loading tasks: " + deviceRegistry.keySet());

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String id = row.getCell(0).getStringCellValue().trim();
                String action = row.getCell(2).getStringCellValue();
                LocalDateTime time = LocalDateTime.parse(row.getCell(3).getStringCellValue(), FORMATTER);
                String repeat = row.getCell(4).getStringCellValue();

                Device device = deviceRegistry.get(id);
                if (device == null) {
                    System.out.println("⚠️ No registered device for ID " + id + ". Available devices: " + deviceRegistry.keySet());
                    continue;
                }

                scheduledTasks.add(new ScheduledTask(device, action, time, repeat));
            }

            System.out.println("✅ Loaded " + scheduledTasks.size() + " scheduled task(s) from Excel.");
        } catch (IOException e) {
            System.err.println("❌ Failed to load scheduled tasks: " + e.getMessage());
        }
    }
    // 🔹 Updates an existing scheduled task
    public void updateTask(int index, LocalDateTime newTime, String newRepeat) {
        if (index < 0 || index >= scheduledTasks.size()) {
            System.out.println("❌ Invalid task index.");
            return;
        }

        ScheduledTask task = scheduledTasks.get(index);
        task.setTime(newTime);
        task.setRepeat(newRepeat);

        saveTasksToExcel();  // ✅ Persist changes
        System.out.println("✅ Task updated successfully: " + task);
    }
    // 🔹 Removes any conflicting scheduled tasks for a device
    public void removeTaskIfConflicts(String deviceId, String action) {
        Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();

            // ✅ Conflicts happen when trying to turn ON/OFF but task has the opposite action
            boolean conflictingAction = !task.getAction().equalsIgnoreCase(action) && task.getDevice().getId().equals(deviceId);

            if (conflictingAction) {
                System.out.println("⚠️ Removing conflicting task: " + task);
                iterator.remove();
            }
        }

        saveTasksToExcel();  // ✅ Persist changes to ensure full system synchronization
    }



}
