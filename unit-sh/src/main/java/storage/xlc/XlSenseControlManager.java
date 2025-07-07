package storage.xlc;

import devices.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sensors.*;
import utils.Log;

import java.io.*;
import java.util.*;
import static storage.xlc.XlWorkbookUtils.*;

public class XlSenseControlManager {

    private static final String SHEET_SENSE = "Sense_Control";

    public static boolean appendToSenseControlSheet(Device slave, Sensor master) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSE);
            if (sheet == null) {
                sheet = workbook.createSheet(SHEET_SENSE);
                createSheetWithHeaders(workbook, SHEET_SENSE,
                        "SLAVE_TYPE", "SLAVE_ID", "SENSOR_TYPE", "SENSOR_ID", "AUTO_ON", "AUTO_OFF");
            }

            removeRowIfExists(sheet, slave.getId());
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);

            row.createCell(0).setCellValue(slave.getType().toString());
            row.createCell(1).setCellValue(slave.getId());
            row.createCell(2).setCellValue(master.getSensorType().toString());
            row.createCell(3).setCellValue(master.getSensorId());
            row.createCell(4).setCellValue(slave.getAutoOnThreshold());
            row.createCell(5).setCellValue(slave.getAutoOffThreshold());

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
            }

            Log.debug("✅ Linked " + slave.getId() + " → " + master.getSensorId());
            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to append to Sense_Control: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeFromSenseControlSheet(String slaveId) {
        return updateWorkbook((tasks, devices, sensors, senseControl) -> {
            for (Row row : senseControl) {
                if (row.getRowNum() == 0) continue;
                if (getCellValue(row, 1).equalsIgnoreCase(slaveId)) {
                    senseControl.removeRow(row);
                    break;
                }
            }
        });
    }

    public static void loadSensorLinksFromExcel(Map<String, Device> devices, Map<String, Sensor> sensors) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSE);
            if (sheet == null) return;

            Set<String> linked = new HashSet<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String slaveId = getCellValue(row, 1);
                String sensorId = getCellValue(row, 3);

                Device device = devices.get(slaveId);
                Sensor sensor = sensors.get(sensorId);

                if (device != null && sensor != null && linked.add(slaveId + "_" + sensorId)) {
                    device.setLinkedSensor(sensor);
                    Log.debug("🔗 Linked " + slaveId + " ↔ " + sensorId);
                }
            }

        } catch (IOException e) {
            Log.error("🔥 Failed to load Sense_Control links: " + e.getMessage());
        }
    }

    public static boolean updateAutoOpThresholds(String deviceId, double newOn, double ignoredOff) {
        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSE);
            if (sheet == null) return false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                if (getCellValue(row, 1).equals(deviceId)) {
                    setCell(row, 4, newOn);
                    setCell(row, 5, newOn);
                    break;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            Log.error("❌ Failed to update AutoOp thresholds: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeSensorLink(String slaveId) {
        File file = getFilePath().toFile();

        if (!file.exists()) {
            System.err.println("❌ Excel file not found.");
            return false;
        }

        if (!isExcelFileHealthy(file)) {
            System.err.println("❌ Excel file is corrupted (bad ZIP). Aborting sensor link removal.");
            return false;
        }

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheet(SHEET_SENSE);
            if (sheet == null) {
                System.err.println("⚠️ Sense_Control sheet missing.");
                return false;
            }

            boolean found = false;
            Iterator<Row> iterator = sheet.iterator();

            while (iterator.hasNext()) {
                Row row = iterator.next();
                Cell cell = row.getCell(1); // SLAVE_ID
                if (cell != null && slaveId.equalsIgnoreCase(cell.getStringCellValue().trim())) {
                    sheet.removeRow(row);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("ℹ️ No mapping found for device: " + slaveId);
                return true;
            }

            File tempFile = new File("shsXl_temp.xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
                System.out.println("✅ Changes written to temporary workbook.");

                if (file.delete()) {
                    if (tempFile.renameTo(file)) {
                        System.out.println("✅ Sensor link removed and Excel file replaced.");
                        return true;
                    } else {
                        System.err.println("⚠️ Temp file rename failed. Original file was deleted.");
                    }
                } else {
                    System.err.println("⚠️ Could not delete original Excel file.");
                }

            } catch (IOException ex) {
                System.err.println("❌ Failed to finalize workbook: " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            System.err.println("🚨 Error modifying Excel: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    private static void removeRowIfExists(Sheet sheet, String slaveId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && getCellValue(row, 1).equalsIgnoreCase(slaveId)) {
                sheet.removeRow(row);
                break;
            }
        }
    }
}
