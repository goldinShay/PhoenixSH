package storage;

import devices.Device;
import devices.DeviceAction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
    private static void saveDevices() {
        System.out.println("🛠️ Saving devices to Excel...");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Devices");

            // 🔹 Write header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("TYPE");
            header.createCell(1).setCellValue("ID");
            header.createCell(2).setCellValue("NAME");
            header.createCell(3).setCellValue("BRAND");
            header.createCell(4).setCellValue("MODEL");
            header.createCell(5).setCellValue("ACTIONS");

            int rowIndex = 1;
            for (Device device : devices.values()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(device.getType().name()); // ✅ Uses DeviceType name
                row.createCell(1).setCellValue(device.getId());
                row.createCell(2).setCellValue(device.getName());
                row.createCell(3).setCellValue(device.getBrand());
                row.createCell(4).setCellValue(device.getModel());
                row.createCell(5).setCellValue(
                        device.getActions().stream()
                                .map(DeviceAction::name)
                                .collect(Collectors.joining(", "))
                );
            }

            System.out.println("📂 Writing to file: " + EXCEL_FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_NAME)) {
                workbook.write(fos);
            }

            System.out.println("✅ Devices saved to Excel successfully.");
        } catch (IOException e) {
            System.out.println("❌ Failed to save devices: " + e.getMessage());
        }
    }

    public static List<Thread> getDeviceThreads() {
        return deviceThreads;
    }
}
