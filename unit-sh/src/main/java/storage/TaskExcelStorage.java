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
    private static final String SHEET_NAME = "DynamicTasks";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void saveTasks(Map<String, List<ScheduledTask>> tasksByDeviceId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Device ID");
            header.createCell(1).setCellValue("Device Name");
            header.createCell(2).setCellValue("Action");
            header.createCell(3).setCellValue("Scheduled Time");
            header.createCell(4).setCellValue("Repeat");

            int rowNum = 1;
            for (List<ScheduledTask> taskList : tasksByDeviceId.values()) {
                for (ScheduledTask task : taskList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(task.getDevice().getId().trim());
                    row.createCell(1).setCellValue(task.getDevice().getName().trim());
                    row.createCell(2).setCellValue(task.getAction().trim());
                    row.createCell(3).setCellValue(task.getTime().format(formatter));
                    row.createCell(4).setCellValue(task.getRepeat().trim());
                }
            }

            try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
                workbook.write(out);
                System.out.println("✅ Tasks saved to Excel successfully.");
            }

        } catch (IOException e) {
            System.out.println("❌ Failed to save tasks: " + e.getMessage());
        }
    }

    public static Map<String, List<ScheduledTask>> loadTasks(Map<String, Device> devices) {
        System.out.println("🔁 loadTasksFromExcel called");
        Map<String, List<ScheduledTask>> tasksByDeviceId = new HashMap<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("📭 No previous tasks file found.");
            return tasksByDeviceId;
        }

        boolean updated = false;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                System.out.println("⚠️ Sheet 'DynamicTasks' not found.");
                return tasksByDeviceId;
            }

            System.out.println("📄 Reading sheet: " + SHEET_NAME);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Raw device ID from Excel
                Cell idCell = row.getCell(0);
                String rawId = getStringCell(idCell).trim();
                String normalizedId = normalizeId(rawId).toUpperCase();

                // Auto-update Excel cell if the ID was not normalized
                if (!rawId.equals(normalizedId)) {
                    idCell.setCellValue(normalizedId);
                    updated = true;
                    System.out.println("✏️ Normalized and updated device ID from '" + rawId + "' to '" + normalizedId + "' in Excel.");
                }

                String deviceName = getStringCell(row.getCell(1)).trim();
                String action = getStringCell(row.getCell(2)).trim();
                String timeStr = getStringCell(row.getCell(3)).trim();

                String repeat = "none";
                Cell repeatCell = row.getCell(4);
                if (repeatCell != null) {
                    switch (repeatCell.getCellType()) {
                        case STRING -> repeat = repeatCell.getStringCellValue().trim().toLowerCase();
                        case BOOLEAN -> repeat = repeatCell.getBooleanCellValue() ? "daily" : "none";
                        case NUMERIC -> repeat = (repeatCell.getNumericCellValue() == 1.0) ? "daily" : "none";
                    }
                }

                LocalDateTime scheduledTime = LocalDateTime.parse(timeStr, formatter);

                Device device = devices.get(normalizedId);
                if (device == null) {
                    // Try case-insensitive fallback
                    for (Map.Entry<String, Device> entry : devices.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(normalizedId)) {
                            device = entry.getValue();
                            break;
                        }
                    }
                }

                if (device == null) {
                    System.out.println("⚠️ Excel task references unknown device ID '" + normalizedId + "' — using GenericDevice");
                    device = new GenericDevice(normalizedId, deviceName, "Unknown", Clock.systemDefaultZone());
                }

                ScheduledTask task = new ScheduledTask(device, action, scheduledTime, repeat);
                tasksByDeviceId.computeIfAbsent(normalizedId, k -> new ArrayList<>()).add(task);

                System.out.println("✅ Loaded task: [" + normalizedId + "] " + action + " at " + scheduledTime + " (Repeat: " + repeat + ")");
            }

            // Write changes back if any normalization occurred
            if (updated) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                    System.out.println("💾 Excel file updated with normalized IDs.");
                }
            }

            System.out.println("📥 All tasks loaded from Excel.");

        } catch (IOException e) {
            System.out.println("❌ Failed to load tasks: " + e.getMessage());
        }

        return tasksByDeviceId;
    }




    private static String getStringCell(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
    public static String normalizeId(String id) {
        return id == null ? null : id.trim().toUpperCase();
    }

}
