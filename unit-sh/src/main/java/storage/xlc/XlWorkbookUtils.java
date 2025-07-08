package storage.xlc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class XlWorkbookUtils {

    private static Path filePath = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");

    public static final String SMART_LIGHT_SHEET = "Smart_Light_Control";
    public static final String[] SMART_LIGHT_HEADERS = {
            "MODE_NAME", "R", "G", "B", "IS_DEFAULT",
            "EFFECT_NAME", "TYPE", "PARAMS"
    };

    public static Path getFilePath() {
        return filePath;
    }

    public static void overrideFilePath(Path newPath) {
        filePath = newPath;
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

    public static void ensureSmartLightSheetExists(Workbook workbook) {
        Sheet sheet = workbook.getSheet(SMART_LIGHT_SHEET);
        if (sheet == null) {
            createSheetWithHeaders(workbook, SMART_LIGHT_SHEET, SMART_LIGHT_HEADERS);
        }
    }

    public static boolean ensureFileExists() {
        File file = getFilePath().toFile();

        if (!file.exists()) {
            System.out.println("⚠️ Excel file not found at: " + getFilePath());
            System.out.print("Do you want to create a new Excel file now? (Y/N): ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                try {
                    Workbook workbook = new XSSFWorkbook();
                    createSheetWithHeaders(workbook, "Scheduled Tasks", "TaskID", "DeviceID", "Action", "Time");
                    createSheetWithHeaders(workbook, "Devices", "Type", "ID", "Name", "Brand", "Model", "AutoEnabled", "AutoOn", "AutoOff", "RGB_R", "RGB_G", "RGB_B", "ACTIVE_MODE");
                    createSheetWithHeaders(workbook, "Sensors", "SensorID", "Name", "Type", "CurrentValue");
                    createSheetWithHeaders(workbook, "Sense_Control", "SensorID", "SlaveDeviceID");
                    createSheetWithHeaders(workbook, SMART_LIGHT_SHEET, SMART_LIGHT_HEADERS);

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        workbook.write(fos);
                    }
                    workbook.close();
                    System.out.println("✅ New Excel file created.");
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to create Excel file: " + e.getMessage());
                    return false;
                }
            } else {
                System.out.println("🛑 Startup aborted by user.");
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
        void accept(Sheet tasks, Sheet devices, Sheet sensors, Sheet senseControl, Sheet smartLightControl) throws IOException;
    }

    public static boolean updateWorkbook(MultiSheetConsumer consumer) {
        if (!ensureFileExists()) return false;

        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            ensureSmartLightSheetExists(workbook);

            Sheet tasks = workbook.getSheet("Scheduled Tasks");
            Sheet devices = workbook.getSheet("Devices");
            Sheet sensors = workbook.getSheet("Sensors");
            Sheet senseControl = workbook.getSheet("Sense_Control");
            Sheet smartLightControl = workbook.getSheet(SMART_LIGHT_SHEET);

            if (tasks == null || devices == null || sensors == null || senseControl == null || smartLightControl == null) {
                System.err.println("❌ Missing required sheet(s). Aborting update.");
                return false;
            }

            consumer.accept(tasks, devices, sensors, senseControl, smartLightControl);

            try (FileOutputStream fos = new FileOutputStream(getFilePath().toFile())) {
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
