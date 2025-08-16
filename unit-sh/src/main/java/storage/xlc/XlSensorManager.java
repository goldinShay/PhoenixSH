package storage.xlc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sensors.*;
import storage.xlc.sheetsCommand.AutoOpControlCommand;
import storage.xlc.sheetsCommand.SensorSheetCommand;
import utils.Log;

import java.io.*;
import java.time.Clock;
import java.util.*;

import static storage.xlc.XlWorkbookUtils.*;

public class XlSensorManager {

    private static final Clock clock = Clock.systemDefaultZone();
    private static final String SHEET_SENSORS = "Sensors";
    private static final String SHEET_SENS_CTRL = "Sens_Ctrl";

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

            Map<SensorSheetCommand, Integer> columnMap = SensorSheetCommand.getColumnMap();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String typeStr   = getCellValue(row, columnMap.get(SensorSheetCommand.TYPE));
                    String id        = getCellValue(row, columnMap.get(SensorSheetCommand.ID));
                    String name      = getCellValue(row, columnMap.get(SensorSheetCommand.NAME));
                    String unitStr   = getCellValue(row, columnMap.get(SensorSheetCommand.UNIT));
                    String valueStr  = getCellValue(row, columnMap.get(SensorSheetCommand.CURRENT_VALUE));

                    SensorType type = SensorType.valueOf(typeStr.trim().toUpperCase());
                    MeasurementUnit unit = MeasurementUnit.valueOf(unitStr.trim().toUpperCase());
                    int value = (int) Double.parseDouble(valueStr);

                    Sensor sensor = SensorFactory.createSensor(type, id, name, unit, value, clock);
                    loadedSensors.put(id, sensor);

                    Log.debug("📥 Sensor loaded: " + id + " | " + name + " | " + value + " " + unit.getDisplay());

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
            Map<SensorSheetCommand, Integer> columnMap = SensorSheetCommand.getColumnMap();

            row.createCell(columnMap.get(SensorSheetCommand.TYPE)).setCellValue(sensor.getSensorType().toString());
            row.createCell(columnMap.get(SensorSheetCommand.ID)).setCellValue(sensor.getSensorId());
            row.createCell(columnMap.get(SensorSheetCommand.NAME)).setCellValue(sensor.getSensorName());
            row.createCell(columnMap.get(SensorSheetCommand.UNIT)).setCellValue(sensor.getUnit().getDisplay());
            row.createCell(columnMap.get(SensorSheetCommand.CURRENT_VALUE)).setCellValue(sensor.getCurrentValue());
            row.createCell(columnMap.get(SensorSheetCommand.ADDED_TS)).setCellValue(sensor.getCreatedTimestamp());
            row.createCell(columnMap.get(SensorSheetCommand.UPDATED_TS)).setCellValue(sensor.getUpdatedTimestamp());
            row.createCell(columnMap.get(SensorSheetCommand.REMOVED_TS)).setCellValue(sensor.getRemovedTimestamp());

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

    // 🔄 Update sensor control link (Sens_Ctrl) by sensor ID
    public static boolean updateSensorControlLink(Sensor sensor) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENS_CTRL);
            if (sheet == null) {
                Log.warn("⚠️ Sheet 'Sens_Ctrl' not found.");
                return false;
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                Log.warn("⚠️ Missing header row in 'Sens_Ctrl'.");
                return false;
            }

            Map<AutoOpControlCommand, Integer> columnMap = new HashMap<>();

            for (Cell cell : header) {
                String label = cell.getStringCellValue().trim();
                for (AutoOpControlCommand col : AutoOpControlCommand.values()) {
                    if (label.equalsIgnoreCase(col.label())) {
                        columnMap.put(col, cell.getColumnIndex());
                        break;
                    }
                }
            }

            int idCol        = columnMap.getOrDefault(AutoOpControlCommand.SENSOR_ID, -1);
            int crntValCol   = columnMap.getOrDefault(AutoOpControlCommand.CRNT_VAL, -1);
            int updatedTsCol = columnMap.getOrDefault(AutoOpControlCommand.UPDATED_TS, -1);

            if (idCol == -1 || crntValCol == -1 || updatedTsCol == -1) {
                Log.warn("⚠️ Missing one or more expected columns in 'Sens_Ctrl'.");
                return false;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String rowId = getCellValue(row, idCol);
                if (rowId.equals(sensor.getSensorId())) {
                    setCell(row, crntValCol, sensor.getCurrentValue());
                    setCell(row, updatedTsCol, sensor.getUpdatedTimestamp());
                    break;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to update Sens_Ctrl link: " + e.getMessage());
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
        Row header = sheet.createRow(0);
        int col = 0;
        for (SensorSheetCommand column : SensorSheetCommand.values()) {
            header.createCell(col++).setCellValue(column.label());
        }
    }
    public static boolean updateSensorSheet(Sensor sensor) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Sensors");
            if (sheet == null) {
                Log.warn("⚠️ Sheet 'Sensors' not found.");
                return false;
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                Log.warn("⚠️ Missing header row in 'Sensors'.");
                return false;
            }

            Map<String, Integer> columnMap = new HashMap<>();
            for (Cell cell : header) {
                String label = cell.getStringCellValue().trim().toUpperCase();
                columnMap.put(label, cell.getColumnIndex());
            }

            int idCol = columnMap.getOrDefault("ID", -1);
            int valueCol = columnMap.getOrDefault("CURRENT_VALUE", -1);
            int updatedCol = columnMap.getOrDefault("UPDATED_TS", -1);

            if (idCol == -1 || valueCol == -1 || updatedCol == -1) {
                Log.warn("⚠️ Missing expected columns in 'Sensors'.");
                return false;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String rowId = getCellValue(row, idCol);
                if (rowId.equalsIgnoreCase(sensor.getSensorId())) {
                    setCell(row, valueCol, sensor.getCurrentValue());
                    setCell(row, updatedCol, sensor.getUpdatedTimestamp());
                    break;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
            }

            Log.debug("✅ Sensor updated in Sensors sheet: " + sensor.getSensorId());
            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to update Sensors sheet: " + e.getMessage());
            return false;
        }
    }


}