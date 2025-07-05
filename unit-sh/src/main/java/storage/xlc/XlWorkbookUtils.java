package storage.xlc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class XlWorkbookUtils {

    private static final Path FILE_PATH = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");

    public static Path getFilePath() {
        return FILE_PATH;
    }

    public static String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    public static void setCell(Row row, int index, String value) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
    }

    public static void setCell(Row row, int index, boolean value) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
    }

    public static void setCell(Row row, int index, double value) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
    }

    public static int getFirstAvailableRow(Sheet sheet) {
        int rowNum = sheet.getLastRowNum() + 1;
        int maxRows = sheet.getWorkbook().getSpreadsheetVersion().getMaxRows();

        while (rowNum < maxRows) {
            Row r = sheet.getRow(rowNum);
            if (r == null || getCellValue(r, 1).isBlank()) {
                return rowNum;
            }
            rowNum++;
        }
        return -1;
    }

    public static void createSheetWithHeaders(Workbook workbook, String name, String... headers) {
        Sheet sheet = workbook.createSheet(name);
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    public static boolean ensureFileExists() {
        File file = FILE_PATH.toFile();
        if (!file.exists()) {
            System.out.println("⚠️ Excel file not found at: " + FILE_PATH);
            System.out.print("Do you want to create a new Excel file now? (Y/N): ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                try {
                    // You’ll plug in XlCreator.createShsXlFile() here
                    return true;
                } catch (Exception e) {
                    System.err.println("❌ Failed to create Excel file: " + e.getMessage());
                    return false;
                }
            } else {
                System.out.println("🛑 Aborting launch.");
                return false;
            }
        }
        return true;
    }

    public static Workbook getWorkbook(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) file.createNewFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            return WorkbookFactory.create(fis);
        }
    }


    @FunctionalInterface
    public interface MultiSheetConsumer {
        void accept(Sheet tasks, Sheet devices, Sheet sensors, Sheet senseControl) throws IOException;
    }

    public static boolean updateWorkbook(MultiSheetConsumer consumer) {
        if (!ensureFileExists()) return false;

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet tasks = workbook.getSheet("Scheduled Tasks");
            Sheet devices = workbook.getSheet("Devices");
            Sheet sensors = workbook.getSheet("Sensors");
            Sheet senseControl = workbook.getSheet("Sense_Control");

            if (tasks == null || devices == null || sensors == null || senseControl == null) {
                System.err.println("❌ Missing required sheet(s). Aborting update.");
                return false;
            }

            consumer.accept(tasks, devices, sensors, senseControl);

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            System.err.println("❌ Exception during workbook update: " + e.getMessage());
            return false;
        }
    }
    public static boolean isExcelFileHealthy(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            new XSSFWorkbook(fis).close();
            return true;
        } catch (Exception e) {
            System.err.println("❌ Excel health check failed: " + e.getMessage());
            return false;
        }
    }



}
