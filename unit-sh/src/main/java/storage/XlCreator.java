package storage;

import devices.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sensors.Sensor;
import sensors.SensorFactory;
import sensors.SensorType;
import utils.ClockUtil;
import utils.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

public class XlCreator {

    private static final Path FILE_PATH = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");
    private static final Clock clock = ClockUtil.getClock();
    private static final String DEVICES_SHEET = "Devices";
    private static final String TASKS_SHEET = "Scheduled Tasks";
    private static final String SENSORS_SHEET = "Sensors";          // 🆕
    private static final String SENSE_CONTROL_SHEET = "Sense_Control"; // 🆕


    public static List<Device> loadDevicesFromExcel() {
        List<Device> devices = new ArrayList<>();
        Path filePath = getFilePath();

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet deviceSheet = workbook.getSheet("Devices");
            if (deviceSheet == null) {
                System.out.println("⚠️ Devices sheet not found.");
                return devices;
            }

            Clock clock = Clock.systemDefaultZone();

            for (Row row : deviceSheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    String type = getSafeString(row.getCell(0));
                    String id = getSafeString(row.getCell(1));
                    String name = getSafeString(row.getCell(2));
                    String brand = getSafeString(row.getCell(3));
                    String model = getSafeString(row.getCell(4));
                    Cell enableCell = row.getCell(5);
                    boolean autoEnabled = false;

                    if (enableCell != null) {
                        switch (enableCell.getCellType()) {
                            case BOOLEAN -> autoEnabled = enableCell.getBooleanCellValue();
                            case STRING -> autoEnabled = Boolean.parseBoolean(enableCell.getStringCellValue().trim());
                            case NUMERIC -> autoEnabled = enableCell.getNumericCellValue() != 0;
                            default -> autoEnabled = false;
                        }
                    }
                    double autoOn = row.getCell(6).getNumericCellValue();
                    double autoOff = row.getCell(7).getNumericCellValue();

                    Device device = switch (type.toUpperCase()) {
                        case "LIGHT" -> new Light(id, name, clock, false, autoOn, autoOff);
                        case "THERMOSTAT" -> new Thermostat(id, name, clock, false, autoOn, autoOff);
                        case "WASHING_MACHINE" -> new WashingMachine(id, name, clock, false, autoOn, autoOff);
                        case "DRYER" -> new Dryer(id, name, clock, false, autoOn, autoOff);
                        default -> {
                            System.out.printf("⚠️ Unsupported device type at row %d: %s%n", row.getRowNum(), type);
                            yield null;
                        }
                    };


                    if (device == null) continue;

                    device.setBrand(brand);
                    device.setModel(model);
                    device.setAutomationEnabled(autoEnabled);

                    System.out.printf("✅ Loaded device → %s | AutoOp: %b | ON: %.1f | OFF: %.1f | Ref: %s%n",
                            id, autoEnabled, autoOn, autoOff, System.identityHashCode(device));

                    devices.add(device);

                } catch (Exception ex) {
                    System.out.printf("⚠️ Skipped row %d due to error: %s%n", row.getRowNum(), ex.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("🛑 Failed to read devices from Excel: " + e.getMessage());
        }

        return devices;
    }

    // 🧽 Utility: Safely extract string from cell, regardless of null or type
    private static String getSafeString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }


    public static void createShsXlFile() throws IOException {
        Files.createDirectories(FILE_PATH.getParent());

        try (Workbook workbook = new XSSFWorkbook()) {

            // ✅ Create TASKS sheet (unchanged)
            createSheetWithHeaders(workbook, TASKS_SHEET,
                    "DEVICE ID", "DEVICE NAME", "ACTION", "SCHEDULED", "REPEAT");

            // ✅ Create DEVICES sheet (with auto threshold columns)
            createSheetWithHeaders(workbook, DEVICES_SHEET,
                    "TYPE", "ID", "NAME", "BRAND", "MODEL", "AUTO-ENABLE",
                    "AUTO-ON", "AUTO-OFF", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS");

            // ✅ Create SENSORS sheet
            createSheetWithHeaders(workbook, "Sensors",
                    "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNIT_NAME", "DEFAULT_VALUE", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS");

            // ✅ Create SEANCE_CONTROL sheet
            createSheetWithHeaders(workbook, "Sense_Control",
                    "SLAVE_TYPE", "SLAVE_ID", "SENSOR_TYPE", "SENSOR_ID", "AUTO_ON", "AUTO_OFF");

            // 💾 Save everything
            saveWorkbook(workbook);
            System.out.println("✅ Excel file created with sensor and control support at: " + FILE_PATH);
        }
    }

    public static void writeDeviceToExcel(Device device) throws IOException {
        updateWorkbook((tasks, devices, sensors, senseControl) -> {
            int rowNum = getFirstAvailableRow(devices);
            if (rowNum == -1) throw new IOException("Excel row limit reached");
            writeDeviceRow(device, devices.createRow(rowNum));
            System.out.println("✅ Device added: " + device.getName());
        });
    }



    public static boolean updateDevice(Device device) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet deviceSheet = workbook.getSheet("Devices");
            if (deviceSheet == null) {
                Log.warn("🟡 Devices sheet not found.");
                return false;
            }

            for (Row row : deviceSheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String deviceId = getCellStringValue(row, 1);
                if (deviceId != null && device.getId().equalsIgnoreCase(deviceId)) {

                    // 🆔 Basic Details
                    setCellValueSafe(row, 2, device.getName());
                    setCellValueSafe(row, 3, device.getBrand() != null ? device.getBrand() : "N/A");
                    setCellValueSafe(row, 4, device.getModel() != null ? device.getModel() : "N/A");

                    // ⚙️ Auto-Enable
                    setCellValueSafe(row, 5, device.isAutomationEnabled());

                    // 🔘 Thresholds (Always write even if not AutoOp-enabled, unless you want to guard it)
                    setThresholdCell(row, 6, device.getAutoOnThreshold());
                    setThresholdCell(row, 7, device.getAutoOffThreshold());

                    // 🎛️ Optional Actions
                    setCellValueSafe(row, 8, "on, off");

                    // 🕒 Update Timestamp (column 10)
                    setCellValueSafe(row, 10, ZonedDateTime.now().toString());

                    break; // Device found and updated
                }
            }

            // 💾 Save the updated workbook
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            System.out.printf("➡️ Excel updated | %s | AUTO-ON: %.1f | AUTO-OFF: %.1f%n",
                    device.getId(), device.getAutoOnThreshold(), device.getAutoOffThreshold());

            return true;

        } catch (IOException e) {
            Log.error("🛑 Failed to update device in Excel: " + e.getMessage());
            return false;
        }
    }

    // 📁 Utility: Checks if the Excel file is a valid ZIP archive
    public static boolean isExcelFileHealthy(File excelFile) {
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(excelFile)) {
            zip.entries().asIterator().forEachRemaining(entry -> {}); // Just iterate to validate
            return true;
        } catch (IOException e) {
            System.err.println("💣 Excel ZIP integrity check failed: " + e.getMessage());
            return false;
        }
    }


    private static String getCellStringValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell != null ? cell.getStringCellValue() : null;
    }

    private static void setCellValueSafe(Row row, int index, String value) {
        Cell cell = row.getCell(index);
        if (cell == null) cell = row.createCell(index);
        cell.setCellValue(value);
    }

    private static void setCellValueSafe(Row row, int index, boolean value) {
        Cell cell = row.getCell(index);
        if (cell == null) cell = row.createCell(index);
        cell.setCellValue(value);
    }

    private static void setThresholdCell(Row row, int index, double value) {
        Cell cell = row.getCell(index);
        if (cell == null) cell = row.createCell(index);
        if (value == (int) value) cell.setCellValue((int) value);
        else cell.setCellValue(value);
    }



    public static boolean removeDevice(String deviceId) {
        return updateWorkbook((tasks, devices, sensors, senseControl) -> {
            int lastRow = devices.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = devices.getRow(i);
                if (row == null) continue;

                if (getCellValue(row, 1).equals(deviceId)) {
                    devices.removeRow(row);
                    if (i < lastRow) devices.shiftRows(i + 1, lastRow, -1);
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

        row.createCell(5).setCellValue(device.isAutomationEnabled() ? "TRUE" : "FALSE"); // AUTO-ENABLE
        double threshold = device.getAutoOnThreshold();
        row.createCell(6).setCellValue(threshold);  // AUTO-ON
        row.createCell(7).setCellValue(threshold);  // AUTO-OFF = mirror

        List<String> actions = device.getAvailableActions();
        row.createCell(8).setCellValue(String.join(", ", actions));                      // ACTIONS

        row.createCell(9).setCellValue(device.getAddedTimestamp() != null ? device.getAddedTimestamp() : "N/A");
        row.createCell(10).setCellValue(device.getUpdatedTimestamp() != null ? device.getUpdatedTimestamp() : "N/A");
        row.createCell(11).setCellValue(device.getRemovedTimestamp() != null ? device.getRemovedTimestamp() : "N/A");
    }

    public static Workbook getWorkbook(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) file.createNewFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            return WorkbookFactory.create(fis);
        }
    }


    public static boolean writeSensorToExcel(Sensor sensor) {
        try {
            Workbook workbook = getWorkbook(FILE_PATH.toString());
            Sheet sheet = workbook.getSheet("Sensors");

            if (sheet == null) {
                sheet = workbook.createSheet("Sensors");
                createSensorHeaderRow(sheet);
            }

            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(sensor.getSensorType().toString());     // SENSOR_TYPE
            newRow.createCell(1).setCellValue(sensor.getSensorId());                  // SENSOR_ID
            newRow.createCell(2).setCellValue(sensor.getSensorName());                // NAME
            newRow.createCell(3).setCellValue(sensor.getUnit());                      // UNITS
            newRow.createCell(4).setCellValue(sensor.getCurrentValue());              // DEFAULT_VAL
            newRow.createCell(5).setCellValue("");                                    // ACTIONS (optional/blank for now)
            newRow.createCell(6).setCellValue(sensor.getCreatedTimestamp().toString());// ADDED_TS
            newRow.createCell(7).setCellValue("");                                    // UPDATED_TS (placeholder)
            newRow.createCell(8).setCellValue("");                                    // REMOVED_TS (placeholder)

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            workbook.close();
            return true;

        } catch (IOException e) {
            System.out.println("❌ Failed to write sensor to Excel: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSensor(Sensor sensor) {
        try {
            // Step 1: Ensure the Excel file and all sheets exist
            if (!Files.exists(FILE_PATH)) {
                System.out.println("🆕 Excel file not found. Recreating full structure...");
                createShsXlFile(); // ← Your method that creates all 4 sheets
            }

            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile())) {
                workbook = new XSSFWorkbook(fis);
                ensureAllSheetsExist(workbook);

            }

            // Step 2: Guarantee the "Sensors" sheet exists (create if needed)
            Sheet sheet = workbook.getSheet("Sensors");
            if (sheet == null) {
                sheet = workbook.createSheet("Sensors");
                createSheetWithHeaders(workbook, "Sensors",
                        "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNIT_NAME",
                        "DEFAULT_VALUE", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS");
            }

            boolean found = false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Cell idCell = row.getCell(1); // SENSOR_ID

                if (idCell != null && idCell.getStringCellValue().trim().equals(sensor.getSensorId())) {
                    row.getCell(2).setCellValue(sensor.getSensorName());
                    row.getCell(3).setCellValue(sensor.getUnit());
                    row.getCell(4).setCellValue(sensor.getCurrentValue());
                    row.getCell(7).setCellValue(sensor.getUpdatedTimestamp()); // UPDATED_TS is at index 7
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("❌ Sensor not found in Excel for update.");
                return false;
            }

            // Step 3: Save the file
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
                workbook.close();
            }

            return true;

        } catch (IOException e) {
            System.out.println("❌ Failed to update sensor: " + e.getMessage());
            return false;
        }
    }


    public static boolean removeSensor(String sensorId) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Sensors");
            if (sheet == null) {
                System.out.println("⚠️ Sheet 'Sensors' not found.");
                return false;
            }

            int rowIndexToRemove = -1;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                Cell idCell = row.getCell(1); // SENSOR_ID is at column index 1

                if (idCell != null && idCell.getStringCellValue().trim().equals(sensorId)) {
                    rowIndexToRemove = row.getRowNum();
                    break;
                }
            }

            if (rowIndexToRemove == -1) return false;

            removeRow(sheet, rowIndexToRemove);

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            System.out.println("❌ Failed to remove sensor: " + e.getMessage());
            return false;
        }
    }

    private static void ensureAllSheetsExist(Workbook workbook) {
        if (workbook.getSheet("Tasks") == null)
            createSheetWithHeaders(workbook, "Tasks", "DEVICE ID", "DEVICE NAME", "ACTION", "SCHEDULED", "REPEAT");

        if (workbook.getSheet("Devices") == null)
            createSheetWithHeaders(workbook, "Devices", "TYPE", "ID", "NAME", "BRAND", "MODEL",
                    "AUTO-ENABLE", "AUTO-ON", "AUTO-OFF", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS");

        if (workbook.getSheet("Sense_Control") == null)
            createSheetWithHeaders(workbook, "Sense_Control", "SLAVE_TYPE", "SLAVE_ID", "SENSOR_TYPE",
                    "SENSOR_ID", "AUTO_ON", "AUTO_OFF");

        if (workbook.getSheet("Sensors") == null)
            createSheetWithHeaders(workbook, "Sensors", "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNIT_NAME",
                    "DEFAULT_VALUE", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS");
    }


    // ✂️ Helper: Remove a row and shift others up
    private static void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        } else if (rowIndex == lastRowNum) {
            Row removing = sheet.getRow(rowIndex);
            if (removing != null) sheet.removeRow(removing);
        }
    }



    public static void createSensorHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("SENSOR_TYPE");
        header.createCell(1).setCellValue("SENSOR_ID");
        header.createCell(2).setCellValue("NAME");
        header.createCell(3).setCellValue("UNITS");
        header.createCell(4).setCellValue("DEFAULT_VAL");
        header.createCell(5).setCellValue("ACTIONS");
        header.createCell(6).setCellValue("ADDED_TS");
        header.createCell(7).setCellValue("UPDATED_TS");
        header.createCell(8).setCellValue("REMOVED_TS");
    }

    public static boolean appendToSenseControlSheet(Device slave, Sensor master) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Sense_Control");

            // 🧱 Create sheet with headers if missing
            if (sheet == null) {
                sheet = workbook.createSheet("Sense_Control");
                createSheetWithHeaders(workbook, "Sense_Control",
                        "SLAVE_TYPE", "SLAVE_ID", "SENSOR_TYPE", "SENSOR_ID", "AUTO_ON", "AUTO_OFF");
            }

            // 🧹 Remove stale entry if already present
            removeRowIfExists(sheet, slave.getId());

            int lastRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(lastRow);

            // 💬 Identity Columns
            row.createCell(0).setCellValue(slave.getType().toString());
            row.createCell(1).setCellValue(slave.getId());
            row.createCell(2).setCellValue(master.getSensorType().toString());
            row.createCell(3).setCellValue(master.getSensorId());

            // 🔁 Use mirrored threshold logic
            double threshold = slave.getAutoOnThreshold();
            Cell onCell = row.createCell(4);
            Cell offCell = row.createCell(5);

            // 🧮 Format nicely if whole number
            if (threshold == (int) threshold) {
                onCell.setCellValue((int) threshold);
                offCell.setCellValue((int) threshold);
            } else {
                onCell.setCellValue(threshold);
                offCell.setCellValue(threshold);
            }

            // 💾 Save updates
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            System.out.printf("📄 Sense_Control updated → %s linked to %s | ON/OFF: %.1f%n",
                    slave.getId(), master.getSensorId(), threshold);

            return true;

        } catch (IOException e) {
            System.out.println("❌ Failed to append to Sense_Control sheet: " + e.getMessage());
            return false;
        }
    }

    private static void removeRowIfExists(Sheet sheet, String slaveId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row
            Row row = sheet.getRow(i);
            if (row != null && slaveId.equalsIgnoreCase(getCellText(row, 1))) {
                sheet.removeRow(row);
                System.out.println("🧹 Removed existing AutoOp entry for " + slaveId);
                break;
            }
        }
    }
    private static String getCellText(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()); // if expecting IDs, cast to int
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return cell.toString().trim(); // fallback for blanks, formulas, etc.
        }
    }




    public static boolean removeFromSenseControlSheet(String slaveId) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Sense_Control");
            if (sheet == null) return false;

            int rowIndexToRemove = -1;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header
                Cell idCell = row.getCell(1); // SLAVE_ID at column 1
                if (idCell != null && idCell.getStringCellValue().trim().equals(slaveId)) {
                    rowIndexToRemove = row.getRowNum();
                    break;
                }
            }

            if (rowIndexToRemove == -1) return false;

            sheet.shiftRows(rowIndexToRemove + 1, sheet.getLastRowNum(), -1);

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            System.out.println("❌ Failed to remove from Sense_Control sheet: " + e.getMessage());
            return false;
        }
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
        if (!Files.exists(FILE_PATH)) {
            System.out.println("⚠️ Excel file not found at: " + FILE_PATH);
            System.out.print("Do you want to create a new Excel file now? (Y/N): ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                try {
                    createShsXlFile();
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to create Excel file: " + e.getMessage());
                    return false;
                }
            } else {
                System.out.println("🛑 Aborting launch. Please place the Excel file at: " + FILE_PATH);
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    interface MultiSheetConsumer {
        void accept(Sheet tasks, Sheet devices, Sheet sensors, Sheet senseControl) throws IOException;
    }


    private static boolean updateWorkbook(MultiSheetConsumer consumer) {
        if (!ensureFileExists()) return false;

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet tasks = workbook.getSheet(TASKS_SHEET);
            Sheet devices = workbook.getSheet(DEVICES_SHEET);
            Sheet sensors = workbook.getSheet(SENSORS_SHEET);
            Sheet senseControl = workbook.getSheet(SENSE_CONTROL_SHEET);

            if (tasks == null || devices == null || sensors == null || senseControl == null) {
                System.err.println("❌ Missing required sheet(s). Aborting update.");
                return false;
            }

            // ✅ Pass all sheets to the consumer
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

    public static Map<String, Sensor> loadSensors() {
        Map<String, Sensor> loadedSensors = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            System.out.println("🔍 Workbook opened with " + workbook.getNumberOfSheets() + " sheets:");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                System.out.println(" - " + workbook.getSheetName(i));
            }

            Sheet sheet = workbook.getSheet("Sensors");
            if (sheet == null) {
                System.out.println("⚠️ Sheet 'Sensors' not found in Excel.");
                return loadedSensors;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    String typeStr = row.getCell(0).getStringCellValue().trim();
                    String id = row.getCell(1).getStringCellValue().trim();
                    String name = row.getCell(2).getStringCellValue().trim();
                    String unit = row.getCell(3).getStringCellValue().trim();
                    int value = (int) row.getCell(4).getNumericCellValue();

                    SensorType type = SensorType.valueOf(typeStr.toUpperCase());
                    Sensor sensor = SensorFactory.createSensor(type, id, name, unit, value, Clock.systemDefaultZone());

                    loadedSensors.put(id, sensor);

                } catch (Exception e) {
                    System.out.println("❌ Skipping invalid sensor row #" + row.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Failed to load sensors from Excel: " + e.getMessage());
        }

        return loadedSensors;
    }
    public static void loadSensorLinksFromExcel() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet senseControlSheet = workbook.getSheet("Sense_Control");
            if (senseControlSheet == null) {
                Log.warn("⚠️ 'Sense_Control' sheet not found in Excel. Skipping sensor linking.");
                return;
            }

            Map<String, Device> devices = DeviceStorage.getDevices();
            Map<String, Sensor> sensors = SensorStorage.getSensors();

            Set<String> linkedSet = new HashSet<>();

            for (Row row : senseControlSheet) {
                if (row.getRowNum() == 0) continue;

                String slaveId = row.getCell(1).getStringCellValue().trim();
                String sensorId = row.getCell(3).getStringCellValue().trim();

                Device device = devices.get(slaveId);
                Sensor sensor = sensors.get(sensorId);

                if (device != null && sensor != null) {
                    String linkKey = slaveId + "_" + sensorId;
                    if (linkedSet.add(linkKey)) {
                        if (device.getAutomationSensorId() == null ||
                                !device.getAutomationSensorId().equals(sensor.getSensorId())) {
                            device.setLinkedSensor(sensor);
                            Log.debug("[Sense_Control] Restored link: " + slaveId + " ↔ " + sensorId);
                        }
                    }
                } else {
                    Log.warn("[Sense_Control] Could not link: " + slaveId + " or " + sensorId + " not found.");
                }
            }

        } catch (IOException e) {
            Log.error("🔥 Failed to load 'Sense_Control' sheet: " + e.getMessage());
        }
    }
    public static Path getFilePath() {
        return FILE_PATH;
    }
    public static boolean removeSensorLink(String slaveId) {
        File file = new File("shsXl.xlsx");
        if (!file.exists()) {
            System.err.println("❌ Excel file not found.");
            return false;
        }
        if (!isExcelFileHealthy(file)) {
            System.err.println("❌ Excel file is corrupted (bad ZIP). Aborting sensor link removal.");
            return false;
        }

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheet("Sense_Control");
            if (sheet == null) {
                System.err.println("⚠️ Sense_Control sheet missing.");
                return false;
            }

            boolean found = false;
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
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

            // ✨ Try writing the workbook safely
            // Check ZIP structure before proceeding
            if (!isExcelFileHealthy(file)) {
                System.err.println("🚫 Cannot modify Excel file: ZIP structure appears corrupted.");
                return false;
            }

// Write to a temp file first
            File tempFile = new File("shsXl_temp.xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
                System.out.println("✅ Changes successfully written to temporary workbook.");

                // Now safely replace the original only if writing succeeded
                if (file.delete()) {
                    if (tempFile.renameTo(file)) {
                        System.out.println("✅ Sensor link removed and Excel file replaced successfully.");
                        return true;
                    } else {
                        System.err.println("⚠️ Temp file rename failed. Original file was deleted!");
                    }
                } else {
                    System.err.println("⚠️ Could not delete original Excel file.");
                }

                return false; // fallback if any replacement step fails

            } catch (IOException ex) {
                System.err.println("❌ Failed to write or finalize workbook: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }


        } catch (IOException | RuntimeException ex) {
            System.err.println("🚨 Error opening or modifying Excel: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean createNewWorkbook() {
        try {
            createShsXlFile();
            return true;
        } catch (IOException e) {
            System.out.println("❌ Failed to create workbook: " + e.getMessage());
            return false;
        }
    }
    public static boolean updateAutoOpThresholds(String deviceId, double newOn, double _ignoredOff) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Sense_Control");
            if (sheet == null) {
                System.out.println("⚠️ Sheet 'Sense_Control' not found.");
                return false;
            }

            boolean rowFound = false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Cell idCell = row.getCell(1);
                if (idCell != null && deviceId.equals(idCell.getStringCellValue().trim())) {
                    // Ensure cells exist, or create them
                    Cell onCell = row.getCell(4) != null ? row.getCell(4) : row.createCell(4);
                    Cell offCell = row.getCell(5) != null ? row.getCell(5) : row.createCell(5);

                    onCell.setCellValue(newOn);
                    offCell.setCellValue(newOn); // mirror AUTO_ON
                    rowFound = true;
                    break;
                }
            }

            if (!rowFound) {
                System.out.println("⚠️ No matching row found for device ID: " + deviceId);
                return false;
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
                workbook.write(fos);
            }

            return true;

        } catch (IOException e) {
            System.out.println("❌ Failed to update AutoOp thresholds: " + e.getMessage());
            return false;
        }
    }


}





