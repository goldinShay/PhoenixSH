package storage;

import devices.Device;
import devices.DeviceFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class ExcelDeviceLoader {

    public static Map<String, Device> loadDevices(String filePath) {
        Map<String, Device> devices = new HashMap<>();

        try (InputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // First sheet
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false; // Skip header row
                    continue;
                }

                String type = getCellValue(row, 0);
                try {
                    Device device = DeviceFactory.createDeviceByType(type);

                    device.setType(type);
                    device.setId(getCellValue(row, 1));
                    device.setName(getCellValue(row, 2));
                    device.setBrand(getCellValue(row, 3));
                    device.setModel(getCellValue(row, 4));

                    String actionsStr = getCellValue(row, 5);
                    if (actionsStr != null && !actionsStr.isEmpty()) {
                        List<String> actions = Arrays.asList(actionsStr.split("\\s*,\\s*"));
                        device.setActions(actions);
                    } else {
                        device.setActions(new ArrayList<>());
                    }

                    System.out.println("👀 Reading device ID: " + device.getId() + " (" + type + ")");
                    devices.put(device.getId(), device);

                } catch (IllegalArgumentException e) {
                    System.err.println("⚠️ Skipping unknown device type: " + type);
                }
            }

            // ✅ Add this outside the loop but inside the try block
            System.out.println("📦 Loaded device IDs: " + devices.keySet());

        } catch (Exception e) {
            System.err.println("❌ Error reading Excel file: " + e.getMessage());
        }

        return devices;
    }


    private static String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };
    }
}
