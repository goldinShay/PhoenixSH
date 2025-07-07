package storage.xlc;

import devices.*;
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
        updateWorkbook((tasks, devices, sensors, senseControl) -> {
            int rowNum = getFirstAvailableRow(devices);
            if (rowNum == -1) throw new IOException("Excel row limit reached");
            writeDeviceRow(device, devices.createRow(rowNum));
        });
    }

    public static boolean updateDevice(Device device) {
        return updateWorkbook((tasks, devices, sensors, senseControl) -> {
            for (Row row : devices) {
                if (row.getRowNum() == 0) continue;

                String deviceId = getCellValue(row, 1);
                if (device.getId().equalsIgnoreCase(deviceId)) {
                    setCell(row, 2, device.getName());
                    setCell(row, 3, device.getBrand());
                    setCell(row, 4, device.getModel());
                    setCell(row, 5, device.isAutomationEnabled());
                    setCell(row, 6, device.getAutoOnThreshold());
                    setCell(row, 7, device.getAutoOffThreshold());
                    setCell(row, 8, String.join(", ", device.getAvailableActions()));
                    setCell(row, 10, ZonedDateTime.now().toString());
                    return;
                }
            }
            throw new IOException("Device not found: " + device.getId());
        });
    }

    public static boolean removeDevice(String deviceId) {
        return updateWorkbook((tasks, devices, sensors, senseControl) -> {
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
