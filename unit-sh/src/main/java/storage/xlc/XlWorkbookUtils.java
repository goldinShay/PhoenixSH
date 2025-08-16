package storage.xlc;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.xlc.sheetsCommand.*;
import utils.Log;

import javax.swing.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class XlWorkbookUtils {

    private static Path filePath = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");

    public static Path getFilePath() {
        return filePath;
    }

    public static void overrideFilePath(Path newPath) {
        filePath = newPath;
    }

    // 🧾 Read cell safely
    public static String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                return String.valueOf((int) Math.round(cell.getNumericCellValue()));
            case FORMULA: {
                FormulaEvaluator evaluator = row.getSheet().getWorkbook()
                        .getCreationHelper()
                        .createFormulaEvaluator();
                CellValue evaluated = evaluator.evaluate(cell);

                switch (evaluated.getCellType()) {
                    case BOOLEAN:
                        return Boolean.toString(evaluated.getBooleanValue());
                    case NUMERIC:
                        return String.valueOf((int) Math.round(evaluated.getNumberValue()));
                    case STRING:
                        return evaluated.getStringValue().trim();
                    default:
                        return "";
                }
            }
            default:
                return cell.toString().trim();
        }
    }
    // 🧠 Safe setters
    public static void setCell(Row row, int index, String value) {
        row.createCell(index, CellType.STRING).setCellValue(value);
    }

    public static void setCell(Row row, int index, boolean value) {
        row.createCell(index, CellType.BOOLEAN).setCellValue(value);
    }

    public static void setCell(Row row, int index, double value) {
        row.createCell(index, CellType.NUMERIC).setCellValue(value);
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

    // 📦 Generic sheet creator from enum
    public static <E extends Enum<E>> void createEnumSheet(Workbook workbook, String name, Class<E> enumClass) {
        String[] headers = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
        createSheetWithHeaders(workbook, name, headers);
    }

    public static void createSheetWithHeaders(XSSFWorkbook workbook, String sheetName, List<String> headers) {
        if (workbook.getSheet(sheetName) != null) {
            System.out.println("📘 Sheet '" + sheetName + "' already exists. Skipping creation.");
            return; // Don’t recreate
        }

        XSSFSheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        System.out.println("🆕 Sheet '" + sheetName + "' created successfully.");
    }
    public static void createSheetWithHeaders(Workbook workbook, String sheetName, String[] headers) {
        if (workbook.getSheet(sheetName) != null) {
            System.out.println("📘 Sheet '" + sheetName + "' already exists. Skipping creation.");
            return;
        }

        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        System.out.println("🆕 Sheet '" + sheetName + "' created successfully.");
    }


    public static boolean ensureFileExists() {
        File file = getFilePath().toFile();

        if (!file.exists()) {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Excel file not found:\n" + getFilePath() + "\n\nWould you like to create a new file?",
                    "Missing Excel File",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(
                        null,
                        "Smart Home System cannot start without an Excel file.\nStartup aborted.",
                        "Startup Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                createEnumSheet(workbook, "Scheduled_Tasks", ScheduledTasksCommand.class);
                createEnumSheet(workbook, "Devices", DeviceSheetCommand.class);
                createEnumSheet(workbook, "Smart_Light_Control", SmartLightSheetCommand.class);
                createEnumSheet(workbook, "Sensors", SensorSheetCommand.class);
                createEnumSheet(workbook, "Sens_Ctrl", AutoOpControlCommand.class);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                JOptionPane.showMessageDialog(
                        null,
                        "✅ New Excel file successfully created:\n" + getFilePath(),
                        "File Created",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return true;

            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "❌ Failed to create Excel file:\n" + e.getMessage(),
                        "Creation Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        }

        return true;
    }

    // 🔁 Workbook access
    public static Workbook getWorkbook(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) file.createNewFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            return WorkbookFactory.create(fis);
        }
    }
    public static Workbook loadWorkbook() {
        try {
            String path = getFilePath().toString(); // Assuming getFilePath() returns the right path
            return getWorkbook(path);
        } catch (IOException e) {
            Log.error("❌ Failed to load workbook: " + e.getMessage());
            return null;
        }
    }


    @FunctionalInterface
    public interface MultiSheetConsumer {
        void accept(Sheet tasks, Sheet devices, Sheet sensors, Sheet senseControl, Sheet smartLightControl) throws IOException;
    }
    @FunctionalInterface
    public interface WorkbookSheetConsumer {
        void accept(Workbook workbook, Sheet tasks, Sheet devices, Sheet sensors, Sheet senseControl, Sheet smartLightCtrl)
                throws IOException;
    }



    public static boolean updateWorkbook(WorkbookSheetConsumer consumer) throws IOException {
        if (!ensureFileExists()) return false;

        File file = getFilePath().toFile();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // 🔧 Load all necessary sheets
            Sheet tasks           = ensureSheet(workbook, "Scheduled_Tasks", ScheduledTasksCommand.values());
            Sheet devices         = ensureSheet(workbook, "Devices", DeviceSheetCommand.values());
            Sheet sensors         = ensureSheet(workbook, "Sensors", SensorSheetCommand.values()); // if defined
            Sheet senseControl    = ensureSheet(workbook, "AutoOp_Ctrl", null);
            Sheet smartLightCtrl  = ensureSheet(workbook, "Smart_Light_Control", null);

            // 🛠️ Pass the workbook and sheets to the consumer
            consumer.accept(workbook, tasks, devices, sensors, senseControl, smartLightCtrl);

            // 💾 Save changes
            System.out.println("📤 Writing workbook to: " + file);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            System.err.println("❌ Exception during workbook update: " + e.getMessage());
            return false;
        }
    }
    public static Sheet ensureSheet(Workbook workbook, String name, Enum<?>[] headers) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null) {
            sheet = workbook.createSheet(name);

            if (headers != null) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i].toString());
                }
                System.out.println("🆕 Created sheet '" + name + "' with headers.");
            } else {
                System.out.println("🆕 Created sheet '" + name + "' without headers.");
            }
        }
        return sheet;
    }

    // 🧪 Health checker
    public static boolean isExcelFileHealthy(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            new XSSFWorkbook(fis).close();
            return true;
        } catch (Exception e) {
            System.err.println("❌ Excel health check failed: " + e.getMessage());
            return false;
        }
    }

    public static double getSafeNumeric(Cell cell, double fallback) {
        if (cell == null) return fallback;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
                default -> fallback;
            };
        } catch (Exception ex) {
            Log.warn("⚠️ Could not parse cell value to double: " + ex.getMessage());
            return fallback;
        }
    }

    // 💡 Add-on for RGB parsing
    public static int getSafeInt(Cell cell, int fallback) {
        if (cell == null) return fallback;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> Integer.parseInt(cell.getStringCellValue().trim());
                default -> fallback;
            };
        } catch (Exception ex) {
            Log.warn("⚠️ Could not parse cell value to int: " + ex.getMessage());
            return fallback;
        }
    }
    public static void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            fileOut.flush(); // ✅ forces data to be pushed to disk
        }
    }
}
