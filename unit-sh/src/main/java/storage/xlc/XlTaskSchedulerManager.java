package storage.xlc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.Log;

import java.io.*;
import java.util.*;

import static storage.xlc.XlWorkbookUtils.*;

public class XlTaskSchedulerManager {
    private static final String SHEET_TASKS = "Scheduled Tasks";

    public static List<Map<String, String>> loadTasks() {
        List<Map<String, String>> tasks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_TASKS);
            if (sheet == null) return tasks;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Map<String, String> task = new HashMap<>();
                task.put("DEVICE_ID", getCellValue(row, 0));
                task.put("DEVICE_NAME", getCellValue(row, 1));
                task.put("ACTION", getCellValue(row, 2));
                task.put("SCHEDULED", getCellValue(row, 3));
                task.put("REPEAT", getCellValue(row, 4));
                tasks.add(task);
            }

        } catch (IOException e) {
            Log.error("❌ Failed to load tasks: " + e.getMessage());
        }

        return tasks;
    }

    public static boolean addTask(String deviceId, String name, String action, String timestamp, String repeat) {
        return updateWorkbook((tasks, devices, sensors, senseControl, smartLightControl) -> {
            int rowNum = getFirstAvailableRow(tasks);
            Row row = tasks.createRow(rowNum);
            setCell(row, 0, deviceId);
            setCell(row, 1, name);
            setCell(row, 2, action);
            setCell(row, 3, timestamp);
            setCell(row, 4, repeat);
            Log.debug("📆 Task added: " + action + " @ " + timestamp);
        });
    }

    public static boolean updateTask(String deviceId, String newTimestamp, String newRepeat) {
        return updateWorkbook((tasks, devices, sensors, senseControl, smartLightControl) -> {
            for (Row row : tasks) {
                if (row.getRowNum() == 0) continue;
                if (getCellValue(row, 0).equals(deviceId)) {
                    setCell(row, 3, newTimestamp);
                    setCell(row, 4, newRepeat);
                    Log.debug("♻️ Task updated for: " + deviceId);
                    break;
                }
            }
        });
    }

    public static boolean deleteTask(String deviceId) {
        return updateWorkbook((tasks, devices, sensors, senseControl, smartLightControl) -> {
            int lastRow = tasks.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = tasks.getRow(i);
                if (row != null && getCellValue(row, 0).equalsIgnoreCase(deviceId)) {
                    tasks.removeRow(row);
                    if (i < lastRow) {
                        tasks.shiftRows(i + 1, lastRow, -1);
                    }
                    Log.debug("🗑️ Task removed for: " + deviceId);
                    break;
                }
            }
        });
    }
}
