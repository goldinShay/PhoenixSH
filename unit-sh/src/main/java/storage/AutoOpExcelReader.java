package storage;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.xlc.XlWorkbookUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class AutoOpExcelReader {

    public static List<AutoOpRecord> readLinks() {
        List<AutoOpRecord> records = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(XlWorkbookUtils.getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Sense_Control");
            if (sheet == null) return records;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    String slaveId = row.getCell(1).getStringCellValue().trim();
                    String sensorId = row.getCell(3).getStringCellValue().trim();
                    double autoOn = row.getCell(4).getNumericCellValue();
                    double autoOff = row.getCell(5).getNumericCellValue();

                    records.add(new AutoOpRecord(slaveId, sensorId, autoOn, autoOff));
                } catch (Exception e) {
                    System.out.println("⚠️ Skipping row " + row.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Failed to read Excel: " + e.getMessage());
        }

        return records;
    }

    public record AutoOpRecord(String slaveId, String sensorId, double autoOn, double autoOff) {}
    public static List<AutoOpRecord> readLinks(Workbook workbook) {
        List<AutoOpRecord> records = new ArrayList<>();

        Sheet sheet = workbook.getSheet("Sense_Control");
        if (sheet == null) return records;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            try {
                String slaveId = row.getCell(1).getStringCellValue().trim();
                String sensorId = row.getCell(3).getStringCellValue().trim();
                double autoOn = row.getCell(4).getNumericCellValue();
                double autoOff = row.getCell(5).getNumericCellValue();

                records.add(new AutoOpRecord(slaveId, sensorId, autoOn, autoOff));
            } catch (Exception e) {
                System.out.println("⚠️ Skipping row " + row.getRowNum() + ": " + e.getMessage());
            }
        }

        return records;
    }

}
