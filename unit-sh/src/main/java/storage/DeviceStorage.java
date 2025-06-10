package storage;

import devices.Device;
import devices.DeviceAction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeviceStorage {

    private static final String EXCEL_FILE_NAME = "/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx";
    private static final Map<String, Device> devices = new HashMap<>(); // ✅ Persistent device storage
    private static final List<Thread> deviceThreads = new ArrayList<>();

    // 🌟 Initialize storage by loading devices from Excel
    public static void initialize() {
        System.out.println("📂 Loading devices from Excel...");
        XlCreator.loadDevicesFromExcel().forEach(device -> {
            devices.put(device.getId(), device);

            Thread thread = new Thread(device);
            thread.start();  // ✅ Ensure each device runs its own thread
            deviceThreads.add(thread);
        });

        System.out.println("✅ Initialized storage with " + devices.size() + " devices.");
    }

    // 🌟 Get all stored devices
    public static Map<String, Device> getDevices() {
        return devices;
    }

    // 🌟 Add a new device to memory & persist in Excel
    public static void addDevice(Device device) {
        devices.put(device.getId(), device);
        saveDevices(); // ✅ Keep Excel in sync
    }

    // 🌟 Remove a device from memory & Excel
    public static boolean removeDevice(String deviceId) {
        if (devices.containsKey(deviceId)) {
            devices.remove(deviceId);
            saveDevices(); // ✅ Keep Excel in sync
            return true;
        }
        return false;
    }

    // 🌟 Save all devices to Excel
    private static void saveDevices() {;

        System.out.println("🛠️ Saving devices to Excel...");

        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(EXCEL_FILE_NAME)) {
            Sheet sheet = workbook.createSheet("Devices");

            // 🔹 Write header row dynamically to prevent future updates from missing fields
            String[] headers = {"TYPE", "ID", "NAME", "BRAND", "MODEL", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (Device device : devices.values()) {
                System.out.println("🛠️ Debug - Saving Added Timestamp for " + device.getName() + ": " + device.getAddedTimestamp()); // ✅ Verify timestamp before saving

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(device.getType().name());
                row.createCell(1).setCellValue(device.getId());
                row.createCell(2).setCellValue(device.getName());
                row.createCell(3).setCellValue(device.getBrand() != null ? device.getBrand() : "N/A");
                row.createCell(4).setCellValue(device.getModel() != null ? device.getModel() : "N/A");

                Cell actionCell = row.createCell(5);
                actionCell.setCellType(CellType.STRING);
                actionCell.setCellValue(String.join(", ", device.getAvailableActions()).trim());

                // 🌟 Ensure timestamp gets written properly
                String addedTs = device.getAddedTimestamp();
                System.out.println("🛠️ Debug - Writing ADDED_TS: " + addedTs); // 🔥 Extra verification before saving

                row.createCell(6).setCellValue(addedTs != null && !addedTs.isEmpty() ? addedTs : "N/A");
                row.createCell(7).setCellValue(device.getUpdatedTimestamp() != null ? device.getUpdatedTimestamp() : "N/A");
                row.createCell(8).setCellValue(device.getRemovedTimestamp() != null ? device.getRemovedTimestamp() : "N/A");
            }




            // 💾 Write workbook contents to file
            workbook.write(fos);
            System.out.println("✅ Devices saved to Excel successfully.");
        } catch (IOException e) {
            System.err.println("❌ Failed to save devices: " + e.getMessage());
        }
    }



    public static List<Thread> getDeviceThreads() {
        return deviceThreads;
    }
}
