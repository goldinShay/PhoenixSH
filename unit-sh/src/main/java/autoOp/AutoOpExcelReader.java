package autoOp;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.xlc.XlWorkbookUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class AutoOpExcelReader {

    private static final String SHEET_NAME = "Sens_Ctrl";
    private static final int COL_LINKED_DEVICE_ID = 0;
    private static final int COL_LINKED_DEVICE_NAME = 1;
    private static final int COL_THRESHOLD = 2;
    private static final int COL_CURRENT_VALUE = 3;
    private static final int COL_SENSOR_NAME = 4;
    private static final int COL_SENSOR_ID = 5;

    public static List<AutoOpRecord> readLinks() {
        try (FileInputStream fis = new FileInputStream(XlWorkbookUtils.getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            return readLinks(workbook);
        } catch (IOException e) {
            System.out.println("❌ Failed to read Excel file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<AutoOpRecord> readLinks(Workbook workbook) {
        List<AutoOpRecord> records = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_NAME);
        if (sheet == null) {
            System.out.println("⚠️ Sheet '" + SHEET_NAME + "' not found in workbook.");
            return records;
        }

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            try {
                String deviceId = getCellAsString(row.getCell(COL_LINKED_DEVICE_ID));
                String sensorId = getCellAsString(row.getCell(COL_SENSOR_ID));
                double autoOn = getCellAsDouble(row.getCell(COL_THRESHOLD));
                double autoOff = getCellAsDouble(row.getCell(COL_THRESHOLD)); // Could differentiate if needed

                records.add(new AutoOpRecord(deviceId, sensorId, autoOn, autoOff));
            } catch (Exception e) {
                System.out.printf("⚠️ Skipping row #%d: %s%n", row.getRowNum(), e.getMessage());
            }
        }

        return records;
    }

    private static String getCellAsString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private static double getCellAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.STRING) {
            return Double.parseDouble(cell.getStringCellValue().trim());
        }
        return cell.getNumericCellValue();
    }

    public record AutoOpRecord(String linkedDeviceId, String sensorId, double autoOn, double autoOff) {}
}