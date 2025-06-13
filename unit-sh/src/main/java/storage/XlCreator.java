package storage;

import devices.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;
import utils.ClockUtil;
import utils.NotificationService;

public class XlCreator {

    private static final Path FILE_PATH = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");
    private static final Clock clock = ClockUtil.getClock();
    private static final String DEVICES_SHEET = "Devices";
    private static final String TASKS_SHEET = "Scheduled Tasks";

    public static List<Device> loadDevicesFromExcel() {
        if (!ensureFileExists()) {
            System.err.println("❌ Error: Excel file does not exist!");
            return Collections.emptyList();
        }

        try (Workbook workbook = openWorkbook()) {
            Sheet sheet = workbook.getSheet(DEVICES_SHEET);
            if (sheet == null) {
                System.err.println("❌ Error: Sheet '" + DEVICES_SHEET + "' not found!");
                return Collections.emptyList();
            }

            List<Device> devices = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                try {
                    String type = row.getCell(0).getStringCellValue().trim().toUpperCase();
                    String id = row.getCell(1).getStringCellValue().trim();
                    String name = row.getCell(2).getStringCellValue().trim();

                    if (!DeviceType.isValidType(type)) {
                        System.err.println("⚠️ Skipping row " + row.getRowNum() + ": Invalid device type '" + type + "'");
                        continue;
                    }

                    DeviceType deviceType = DeviceType.valueOf(type);
                    Device device;

                    Clock clock = Clock.systemDefaultZone();
                    NotificationService ns = new NotificationService();

                    // ✅ Dynamically create the correct device type
                    switch (deviceType) {
                        case LIGHT -> device = new Light(id, name, clock, false);
                        case THERMOSTAT -> device = new Thermostat(id, name, 25.0, ns, clock);
                        default -> {
                            System.err.println("⚠️ Unsupported device type: " + type);
                            continue;
                        }
                    }

                    devices.add(device);
                    System.out.println("✅ Successfully Loaded: " + id + " (" + type + ")");
                } catch (IllegalArgumentException e) {
                    System.err.println("⚠️ Skipping row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
            return devices;
        } catch (IOException e) {
            System.err.println("❌ Failed to read devices: " + e.getMessage());
            return Collections.emptyList();
        }
    }




    public static void createShsXlFile() throws IOException {
        Files.createDirectories(FILE_PATH.getParent());

        try (Workbook workbook = new XSSFWorkbook()) {
            // ✅ Create TASKS sheet (unchanged)
            createSheetWithHeaders(workbook, TASKS_SHEET, "DEVICE ID", "DEVICE NAME", "ACTION", "SCHEDULED", "REPEAT");

            // ✅ Create DEVICES sheet with new format
            createSheetWithHeaders(workbook, DEVICES_SHEET,
                    "TYPE", "ID", "NAME", "BRAND", "MODEL", "ACTIONS", "AUTO-ON", "AUTO-OFF",  // 🔥 Added Auto-Enabler Columns!
                    "ADDED_TS", "UPDATED_TS", "REMOVED_TS");

            saveWorkbook(workbook);
            System.out.println("✅ Excel file created with Auto-Enabler support at: " + FILE_PATH);
        }
    }



    public static void writeDeviceToExcel(Device device) {
        updateWorkbook(sheet -> {
            int rowNum = getFirstAvailableRow(sheet);
            if (rowNum == -1) throw new IOException("Excel row limit reached");
            writeDeviceRow(device, sheet.createRow(rowNum));
            System.out.println("✅ Device added: " + device.getName());
        });
    }

    public static boolean updateDevice(Device updatedDevice) {
        return updateWorkbook(sheet -> {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                if (getCellValue(row, 1).equals(updatedDevice.getId())) {
                    writeDeviceRow(updatedDevice, row);
                    System.out.println("✅ Device updated: " + updatedDevice.getId());
                    return;
                }
            }
            throw new IOException("Device not found: " + updatedDevice.getId());
        });
    }

    public static boolean removeDevice(String deviceId) {
        return updateWorkbook(sheet -> {
            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                if (getCellValue(row, 1).equals(deviceId)) {
                    sheet.removeRow(row);
                    if (i < lastRow) sheet.shiftRows(i + 1, lastRow, -1);
                    System.out.println("✅ Device removed: " + deviceId);
                    return;
                }
            }
            throw new IOException("Device not found: " + deviceId);
        });
    }

    public static String getNextAvailableId(String typePrefix, Set<String> existingIds) {

        int maxId = existingIds.stream()
                .filter(id -> id.startsWith(typePrefix))
                .map(id -> id.replace(typePrefix, ""))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        String newId = typePrefix + String.format("%03d", maxId + 1);
        System.out.println("✅ Generated New ID: " + newId); // 🔥 Debug confirmation
        return newId;
    }


    // Internal helpers
    private static void writeDeviceRow(Device device, Row row) {
        row.createCell(0).setCellValue(device.getType().name());
        row.createCell(1).setCellValue(device.getId());
        row.createCell(2).setCellValue(device.getName());
        row.createCell(3).setCellValue(device.getBrand() != null ? device.getBrand() : "N/A");
        row.createCell(4).setCellValue(device.getModel() != null ? device.getModel() : "N/A");

        // 🌟 Ensure actions are stored correctly
        List<String> actions = device.getAvailableActions();

        row.createCell(5).setCellValue(String.join(", ", actions));

        // 🌟 Timestamp entries
        row.createCell(6).setCellValue(device.getAddedTimestamp() != null ? device.getAddedTimestamp() : "N/A");
        row.createCell(7).setCellValue(device.getUpdatedTimestamp() != null ? device.getUpdatedTimestamp() : "N/A");
        row.createCell(8).setCellValue(device.getRemovedTimestamp() != null ? device.getRemovedTimestamp() : "N/A");
    }



    private static Device parseDeviceRow(Row row) {
        if (row == null) {
            System.err.println("❌ Error: Received a null row.");
            return null;
        }

        String type = getCellValue(row, 0).trim().toUpperCase();
        String id = getCellValue(row, 1);
        String name = getCellValue(row, 2);

        // 🔍 Debugging Type Extraction
        System.out.println("👉 Type from Excel = '" + type + "' (length: " + type.length() + ")");

        if (type.isBlank() || id.isBlank() || name.isBlank()) {
            System.err.println("⚠️ Skipping row " + row.getRowNum() + ": Missing essential data (Type: " + type + ", ID: " + id + ", Name: " + name + ")");
            return null;
        }

        Device device = DeviceFactory.createDeviceByType(type, id, name, clock, DeviceFactory.getDevices());

        if (device == null) {
            System.err.println("❌ Device creation failed for type: " + type);
            return null;
        }

        // Setting additional device properties
        device.setBrand(getCellValue(row, 3));
        device.setModel(getCellValue(row, 4));

        // 🔄 Handling Actions
        String actionStr = getCellValue(row, 5);
        if (actionStr != null && !actionStr.trim().isEmpty()) {
            try {
                List<DeviceAction> actions = Arrays.stream(actionStr.split(","))
                        .map(String::trim)
                        .map(DeviceAction::fromString)
                        .toList();
                device.setActions(actions);
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Failed to parse actions for device ID " + id + " → " + e.getMessage());
            }
        }

        System.out.println("✅ Successfully parsed device: " + device.getId() + " (" + device.getType() + ")");
        return device;
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

    private static int getFirstAvailableRow(Sheet sheet) {
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

    private static void createSheetWithHeaders(Workbook workbook, String name, String... headers) {
        Sheet sheet = workbook.createSheet(name);
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    private static Workbook openWorkbook() throws IOException {
        return new XSSFWorkbook(new FileInputStream(FILE_PATH.toFile()));
    }

    private static void saveWorkbook(Workbook workbook) throws IOException {
        try (FileOutputStream out = new FileOutputStream(FILE_PATH.toFile())) {
            workbook.write(out);
        }
    }

    private static boolean ensureFileExists() {
        try {
            if (!Files.exists(FILE_PATH)) createShsXlFile();
            return true;
        } catch (IOException e) {
            System.err.println("❌ Unable to prepare file: " + e.getMessage());
            return false;
        }
    }

    private static boolean updateWorkbook(WorkbookConsumer consumer) {
        if (!ensureFileExists()) return false;
        try (Workbook workbook = openWorkbook()) {
            Sheet sheet = workbook.getSheet(DEVICES_SHEET);
            consumer.accept(sheet);
            saveWorkbook(workbook);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Operation failed: " + e.getMessage());
            return false;
        }
    }

    @FunctionalInterface
    private interface WorkbookConsumer {
        void accept(Sheet sheet) throws IOException;
    }
}
