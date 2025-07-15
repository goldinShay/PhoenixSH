package storage;

import devices.Device;
import devices.GenericDevice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scheduler.ScheduledTask;

import java.io.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskExcelStorage {

    private static final String FILE_NAME = "tasks.xlsx";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Save all tasks grouped by deviceId into Excel
    public static void saveTasks(Map<String, List<ScheduledTask>> tasksByDeviceId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            for (Map.Entry<String, List<ScheduledTask>> entry : tasksByDeviceId.entrySet()) {
                String deviceId = entry.getKey();
                List<ScheduledTask> tasks = entry.getValue();

                Sheet sheet = workbook.createSheet(deviceId);

                // Header row
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Device ID");
                header.createCell(1).setCellValue("Device Name");
                header.createCell(2).setCellValue("Action");
                header.createCell(3).setCellValue("Scheduled Time");
                header.createCell(4).setCellValue("Repeat");

                int rowNum = 1;
                for (ScheduledTask task : tasks) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(task.getDevice().getId());
                    row.createCell(1).setCellValue(task.getDevice().getName());
                    row.createCell(2).setCellValue(task.getAction());
                    row.createCell(3).setCellValue(task.getTime().format(formatter));
                    row.createCell(4).setCellValue(task.getRepeat());
                }
            }

            try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
                workbook.write(out);
            }

        } catch (IOException e) {
            System.out.println("❌ Failed to save tasks: " + e.getMessage());
        }
    }

    // ✅ REFACTORED: Load tasks using preloaded devices map
    public static Map<String, List<ScheduledTask>> loadTasks(Map<String, Device> devices) {
        Map<String, List<ScheduledTask>> tasksBySheetName = new HashMap<>();

        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("📭 No previous tasks file found.");
            return tasksBySheetName;
        }

        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (Sheet sheet : workbook) {
                String sheetDeviceId = sheet.getSheetName();  // Used as grouping key
                List<ScheduledTask> tasks = new ArrayList<>();

                System.out.println("📄 Reading sheet for device: " + sheetDeviceId);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String deviceId = row.getCell(0).getStringCellValue();
                    String deviceName = row.getCell(1).getStringCellValue();
                    String action = row.getCell(2).getStringCellValue();
                    String timeStr = row.getCell(3).getStringCellValue();
                    Cell repeatCell = row.getCell(4);

                    String repeat;
                    if (repeatCell.getCellType() == CellType.STRING) {
                        repeat = repeatCell.getStringCellValue().toLowerCase();
                    } else if (repeatCell.getCellType() == CellType.BOOLEAN) {
                        repeat = repeatCell.getBooleanCellValue() ? "daily" : "none";
                    } else if (repeatCell.getCellType() == CellType.NUMERIC) {
                        repeat = (repeatCell.getNumericCellValue() == 1.0) ? "daily" : "none";
                    } else {
                        repeat = "none";
                    }

                    LocalDateTime scheduledTime = LocalDateTime.parse(timeStr, formatter);

                    // 🔄 Use preloaded device map
                    Device realDevice = devices.get(deviceId);
                    Device dummyDevice = (realDevice != null)
                            ? realDevice
                            : new GenericDevice(deviceId, deviceName, "Unknown", Clock.systemDefaultZone());

                    ScheduledTask task = new ScheduledTask(dummyDevice, action, scheduledTime, repeat);
                    tasks.add(task);

                    System.out.println("✅ Loaded task: [" + deviceId + "] " + action + " at " + scheduledTime + " (Repeat: " + repeat + ")");
                }

                tasksBySheetName.put(sheetDeviceId, tasks);
            }

            System.out.println("📥 All tasks loaded from Excel.");
        } catch (IOException e) {
            System.out.println("❌ Failed to load tasks: " + e.getMessage());
        }

        return tasksBySheetName;
    }

}
