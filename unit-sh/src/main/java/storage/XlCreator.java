package storage;

import devices.Device;
import devices.DeviceAction;
import devices.DeviceFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class XlCreator {

    private static final Path FILE_PATH = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");
    private static final String SHEET_NAME = "Devices";

    // ✅ Create a new Excel file with headers
    public static void createShsXlFile() throws IOException {
        // Ensure parent directories exist
        Files.createDirectories(FILE_PATH.getParent());

        Workbook workbook = new XSSFWorkbook();

        // Sheet 1: ScheduledTasks
        Sheet scheduledSheet = workbook.createSheet("ScheduledTasks");
        Row scheduledHeader = scheduledSheet.createRow(0);
        String[] scheduledCols = {"DEVICE ID", "DEVICE NAME", "ACTION", "SCHEDULED", "REPEAT"};
        for (int i = 0; i < scheduledCols.length; i++) {
            scheduledHeader.createCell(i).setCellValue(scheduledCols[i]);
        }

        // Sheet 2: Devices
        Sheet devicesSheet = workbook.createSheet(SHEET_NAME);
        Row devicesHeader = devicesSheet.createRow(0);
        String[] deviceCols = {"TYPE", "ID", "NAME", "BRAND", "MODEL", "ACTIONS"};
        for (int i = 0; i < deviceCols.length; i++) {
            devicesHeader.createCell(i).setCellValue(deviceCols[i]);
        }

        try (FileOutputStream out = new FileOutputStream(FILE_PATH.toFile())) {
            workbook.write(out);
            System.out.println("✅ Excel file created at: " + FILE_PATH);
        } finally {
            workbook.close();
        }
    }

    // ✅ Read all devices
    public static List<Device> readDevices() {
        List<Device> devices = new ArrayList<>();

        try {
            if (!Files.exists(FILE_PATH)) {
                createShsXlFile();
            }

            try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) return devices;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String type = getCellValue(row, 0);
                    String id = getCellValue(row, 1);
                    String name = getCellValue(row, 2);

                    try {
                        Device device = DeviceFactory.createDeviceByType(type, id, name);
                        device.setType(type);
                        device.setBrand(getCellValue(row, 3));
                        device.setModel(getCellValue(row, 4));

                        String actionStr = getCellValue(row, 5);
                        if (actionStr != null && !actionStr.isBlank()) {
                            List<DeviceAction> actions = Arrays.stream(actionStr.split(","))
                                    .map(String::trim)
                                    .map(DeviceAction::fromString)
                                    .toList();
                            device.setActions(actions);
                        }

                        devices.add(device);
                    } catch (IllegalArgumentException e) {
                        System.err.println("⚠️ Skipping row " + i + ": " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to read devices: " + e.getMessage());
        }

        return devices;
    }

    // ✅ Add a new device
    public static void writeDeviceToExcel(Device device) {
        try {
            if (!Files.exists(FILE_PATH)) {
                createShsXlFile();
            }

            try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheet(SHEET_NAME);
                int rowNum = 1;
                while (sheet.getRow(rowNum) != null && sheet.getRow(rowNum).getCell(1) != null &&
                        !getCellValue(sheet.getRow(rowNum), 1).isBlank()) {
                    rowNum++;
                }

                if (rowNum > sheet.getLastRowNum() + 1000) {
                    System.err.println("❌ Too many rows — Excel sheet may be corrupted.");
                    return;
                }

                Row row = sheet.createRow(rowNum);

                row.createCell(0).setCellValue(device.getType());
                row.createCell(1).setCellValue(device.getId());
                row.createCell(2).setCellValue(device.getName());
                row.createCell(3).setCellValue(device.getBrand());
                row.createCell(4).setCellValue(device.getModel());
                row.createCell(5).setCellValue(
                        device.getActions().stream()
                                .map(DeviceAction::name)
                                .collect(Collectors.joining(", "))
                );

                try (FileOutputStream out = new FileOutputStream(FILE_PATH.toFile())) {
                    workbook.write(out);
                    System.out.println("✅ Device added: " + device.getName());
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to write device: " + e.getMessage());
        }
    }

    // ✅ Update an existing device
    public static boolean updateDevice(Device updatedDevice) {
        try {
            if (!Files.exists(FILE_PATH)) {
                createShsXlFile();
            }

            try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheet(SHEET_NAME);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String id = getCellValue(row, 1);
                    if (id.equals(updatedDevice.getId())) {
                        row.getCell(0).setCellValue(updatedDevice.getType());
                        row.getCell(2).setCellValue(updatedDevice.getName());
                        row.getCell(3).setCellValue(updatedDevice.getBrand());
                        row.getCell(4).setCellValue(updatedDevice.getModel());
                        row.getCell(5).setCellValue(
                                updatedDevice.getActions().stream()
                                        .map(DeviceAction::name)
                                        .collect(Collectors.joining(", "))
                        );

                        try (FileOutputStream out = new FileOutputStream(FILE_PATH.toFile())) {
                            workbook.write(out);
                            System.out.println("✅ Device updated: " + updatedDevice.getId());
                            return true;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to update device: " + e.getMessage());
        }

        return false;
    }

    // ✅ Remove a device by ID
    public static boolean removeDevice(String deviceId) {
        boolean removed = false;

        try {
            if (!Files.exists(FILE_PATH)) {
                createShsXlFile();
            }

            try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheet(SHEET_NAME);
                int lastRow = sheet.getLastRowNum();

                for (int i = 1; i <= lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String id = getCellValue(row, 1);
                    if (id.equals(deviceId)) {
                        sheet.removeRow(row);
                        if (i != lastRow) {
                            sheet.shiftRows(i + 1, lastRow, -1);
                        }
                        removed = true;
                        break;
                    }
                }

                if (removed) {
                    try (FileOutputStream out = new FileOutputStream(FILE_PATH.toFile())) {
                        workbook.write(out);
                        System.out.println("✅ Device removed: " + deviceId);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to remove device: " + e.getMessage());
        }

        return removed;
    }

    private static String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
