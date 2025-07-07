package storage.xlc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sensors.*;
import utils.Log;

import java.io.*;
import java.time.Clock;
import java.util.*;

import static storage.xlc.XlWorkbookUtils.*;

public class XlSensorManager {

    private static final Clock clock = Clock.systemDefaultZone();
    private static final String SHEET_SENSORS = "Sensors";

    // 🔍 Load sensors from the current Excel file path
    public static Map<String, Sensor> loadSensors() {
        Map<String, Sensor> loadedSensors = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSORS);
            if (sheet == null) {
                Log.warn("⚠️ Sheet 'Sensors' not found.");
                return loadedSensors;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String typeStr = getCellValue(row, 0);
                    String id = getCellValue(row, 1);
                    String name = getCellValue(row, 2);
                    String unit = getCellValue(row, 3);
                    int value = (int) Double.parseDouble(getCellValue(row, 4));

                    SensorType type = SensorType.valueOf(typeStr.toUpperCase());
                    Sensor sensor = SensorFactory.createSensor(type, id, name, unit, value, clock);

                    loadedSensors.put(id, sensor);
                } catch (Exception e) {
                    Log.warn("❌ Skipping invalid sensor row #" + row.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("❌ Failed to load sensors from Excel: " + e.getMessage());
        }

        return loadedSensors;
    }

    // ✍️ Write a new sensor row to Excel
    public static boolean writeSensorToExcel(Sensor sensor) {
        try {
            Workbook workbook = getWorkbook(getFilePath().toString());
            Sheet sheet = workbook.getSheet(SHEET_SENSORS);

            if (sheet == null) {
                sheet = workbook.createSheet(SHEET_SENSORS);
                createSensorHeaderRow(sheet);
            }

            int lastRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(lastRow);

            row.createCell(0).setCellValue(sensor.getSensorType().toString());
            row.createCell(1).setCellValue(sensor.getSensorId());
            row.createCell(2).setCellValue(sensor.getSensorName());
            row.createCell(3).setCellValue(sensor.getUnit());
            row.createCell(4).setCellValue(sensor.getCurrentValue());
            row.createCell(5).setCellValue("");
            row.createCell(6).setCellValue(sensor.getCreatedTimestamp());
            row.createCell(7).setCellValue("");
            row.createCell(8).setCellValue("");

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
                workbook.close();
            }

            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to write sensor: " + e.getMessage());
            return false;
        }
    }

    // 🔄 Update sensor details by ID
    public static boolean updateSensor(Sensor sensor) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSORS);
            if (sheet == null) {
                Log.warn("⚠️ Sheet 'Sensors' not found.");
                return false;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                if (getCellValue(row, 1).equals(sensor.getSensorId())) {
                    setCell(row, 2, sensor.getSensorName());
                    setCell(row, 3, sensor.getUnit());
                    setCell(row, 4, sensor.getCurrentValue());
                    setCell(row, 7, sensor.getUpdatedTimestamp());
                    break;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to update sensor: " + e.getMessage());
            return false;
        }
    }

    // 🗑️ Remove a sensor by ID
    public static boolean removeSensor(String sensorId) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSORS);
            if (sheet == null) return false;

            Iterator<Row> iterator = sheet.iterator();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (row.getRowNum() == 0) continue;
                if (getCellValue(row, 1).equalsIgnoreCase(sensorId)) {
                    sheet.removeRow(row);
                    break;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to remove sensor: " + e.getMessage());
            return false;
        }
    }

    // 🧾 Add headers if sheet is created manually
    private static void createSensorHeaderRow(Sheet sheet) {
        String[] headers = {
                "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNITS",
                "DEFAULT_VAL", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS"
        };
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }
}
