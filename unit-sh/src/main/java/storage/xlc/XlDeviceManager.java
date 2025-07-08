package storage.xlc;

import devices.*;
import devices.actions.SmartLightAction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DeviceStorage;
import utils.Log;
import utils.ClockUtil;

import java.io.*;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

import static storage.xlc.XlWorkbookUtils.*;

public class XlDeviceManager {
    // 🧭 Excel Column Indexes (Devices Sheet)
    private static final int COL_TYPE = 0;
    private static final int COL_ID = 1;
    private static final int COL_NAME = 2;
    private static final int COL_BRAND = 3;
    private static final int COL_MODEL = 4;
    private static final int COL_AUTO_ENABLED = 5;
    private static final int COL_AUTO_ON = 6;
    private static final int COL_AUTO_OFF = 7;
    private static final int COL_ACTIONS = 8;
    private static final int COL_ADDED_TS = 9;
    private static final int COL_UPDATED_TS = 10;
    private static final int COL_REMOVED_TS = 11;
    private static final int COL_RED = 12;
    private static final int COL_GREEN = 13;
    private static final int COL_BLUE = 14;
    private static final int COL_MODE_LABEL = 15;


    private static final Path FILE_PATH = XlWorkbookUtils.getFilePath();
    private static final Clock clock = ClockUtil.getClock();
    private static final String DEVICES_SHEET = "Devices";

    public static List<Device> loadDevicesFromExcel() {
        List<Device> devices = new ArrayList<>();

        Log.debug("📁 Loading devices from Excel file: " + FILE_PATH); // 🔍 Active file path debug

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet deviceSheet = workbook.getSheet(DEVICES_SHEET);
            if (deviceSheet == null) {
                Log.warn("⚠️ Devices sheet not found.");
                return devices;
            }

            for (Row row : deviceSheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String type = getCellValue(row, 0);
                    String id = getCellValue(row, 1);
                    String name = getCellValue(row, 2);
                    String brand = getCellValue(row, 3);
                    String model = getCellValue(row, 4);
                    boolean autoEnabled = parseBoolean(row.getCell(5));
                    double autoOn = row.getCell(6).getNumericCellValue();
                    double autoOff = row.getCell(7).getNumericCellValue();

                    Log.debug("🔍 Parsing row → TYPE: [" + type + "], ID: [" + id + "], NAME: [" + name + "]");

                    Map<String, Device> deviceMap = DeviceStorage.getDevices();
                    Device device = DeviceFactory.createDeviceByType(type, id, name, clock, deviceMap);

                    if (device == null) {
                        Log.warn("⚠️ Device creation failed for type: [" + type + "], ID: [" + id + "]");
                        continue;
                    }

                    device.setBrand(brand);
                    device.setModel(model);
                    device.setAutomationEnabled(autoEnabled);

                    devices.add(device);

                } catch (Exception ex) {
                    Log.warn("⚠️ Skipped row due to error: " + ex.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("🛑 Failed to read devices from Excel: " + e.getMessage());
        }

        Log.info("📦 Finished loading devices. Count: " + devices.size());
        return devices;
    }



    public static void writeDeviceToExcel(Device device) throws IOException {
        updateWorkbook((tasks, devices, sensors, senseControl, smartLightControl) -> {
            int rowNum = getFirstAvailableRow(devices);
            if (rowNum == -1) throw new IOException("Excel row limit reached");
            writeDeviceRow(device, devices.createRow(rowNum));
        });
    }

    public static boolean updateDevice(Device device) {
        return updateWorkbook((tasks, devices, sensors, senseControl, smartLightControl) -> {
            for (Row row : devices) {
                if (row.getRowNum() == 0) continue;

                String rowId = getCellValue(row, COL_ID);
                if (device.getId().equalsIgnoreCase(rowId)) {
                    setCell(row, COL_NAME, device.getName());
                    setCell(row, COL_BRAND, device.getBrand());
                    setCell(row, COL_MODEL, device.getModel());
                    setCell(row, COL_AUTO_ENABLED, device.isAutomationEnabled());
                    setCell(row, COL_AUTO_ON, device.getAutoOnThreshold());
                    setCell(row, COL_AUTO_OFF, device.getAutoOffThreshold());
                    setCell(row, COL_ACTIONS, String.join(", ", device.getAvailableActions()));
                    setCell(row, COL_UPDATED_TS, ZonedDateTime.now().toString());

                    if (device instanceof SmartLight smart) {
                        SmartLightAction mode = smart.getLightMode();
                        if (mode != null) {
                            setCell(row, COL_RED, mode.getRed());
                            setCell(row, COL_GREEN, mode.getGreen());
                            setCell(row, COL_BLUE, mode.getBlue());
                            setCell(row, COL_MODE_LABEL, mode.toString());
                        } else {
                            setCell(row, COL_RED, 0);
                            setCell(row, COL_GREEN, 0);
                            setCell(row, COL_BLUE, 0);
                            setCell(row, COL_MODE_LABEL, "None");
                        }
                    }

                    Log.info("✅ Device updated: " + device.getId());
                    return;
                }
            }
            throw new IOException("Device not found in Excel: " + device.getId());
        });
    }


    public static boolean removeDevice(String deviceId) {
        return updateWorkbook((tasks, devices, sensors, senseControl, smartLightControl) -> {
            int lastRow = devices.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = devices.getRow(i);
                if (row != null && getCellValue(row, 1).equals(deviceId)) {
                    devices.removeRow(row);
                    if (i < lastRow) devices.shiftRows(i + 1, lastRow, -1);
                    return;
                }
            }
            throw new IOException("Device not found: " + deviceId);
        });

    }

    public static String getNextAvailableId(String prefix, Set<String> existingIds) {
        int max = existingIds.stream()
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.replace(prefix, ""))
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        return prefix + String.format("%03d", max + 1);
    }

    private static void writeDeviceRow(Device device, Row row) {
        row.createCell(0).setCellValue(device.getType().name());
        row.createCell(1).setCellValue(device.getId());
        row.createCell(2).setCellValue(device.getName());
        row.createCell(3).setCellValue(device.getBrand());
        row.createCell(4).setCellValue(device.getModel());
        row.createCell(5).setCellValue(device.isAutomationEnabled());
        row.createCell(6).setCellValue(device.getAutoOnThreshold());
        row.createCell(7).setCellValue(device.getAutoOffThreshold());
        row.createCell(8).setCellValue(String.join(", ", device.getAvailableActions()));
        row.createCell(9).setCellValue(device.getAddedTimestamp());
        row.createCell(10).setCellValue(device.getUpdatedTimestamp());
        row.createCell(11).setCellValue(device.getRemovedTimestamp());

        // 🆕 RGB support (for SmartLight only)
        if (device instanceof SmartLight smart) {
            SmartLightAction mode = smart.getLightMode();
            if (mode != null) {
                row.createCell(12).setCellValue(mode.getRed());
                row.createCell(13).setCellValue(mode.getGreen());
                row.createCell(14).setCellValue(mode.getBlue());
                row.createCell(15).setCellValue(mode.toString()); // or any label method you add later
            } else {
                row.createCell(12).setCellValue(0);
                row.createCell(13).setCellValue(0);
                row.createCell(14).setCellValue(0);
                row.createCell(15).setCellValue("None");
            }
        }
    }


    private static boolean parseBoolean(Cell cell) {
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> Boolean.parseBoolean(cell.getStringCellValue());
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> false;
        };
    }
}
