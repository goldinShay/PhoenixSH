package storage;

import devices.Device;
import devices.DeviceAction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class DeviceStorage {

    private static final String EXCEL_FILE_NAME = "/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx";
    private static final Map<String, Device> devices = new HashMap<>();
    private static final List<Thread> deviceThreads = new ArrayList<>();

    // 🌟 Initialize storage by loading devices from Excel
    public static void initialize() {
        System.out.println("🛠️ Debug - Initializing DeviceStorage...");
        devices.clear();

        List<Device> loadedDevices = XlCreator.loadDevicesFromExcel();
        for (Device device : loadedDevices) {
            devices.put(device.getId(), device);
        }

        System.out.println("Debug - Devices in DeviceStorage after Excel load: " + devices.keySet());
    }

    // 🌟 Get all stored devices with real-time synchronization
    public static Map<String, Device> getDevices() {
        refreshDevices();  // ✅ Ensures up-to-date data before fetching
        System.out.println("🔎 Debug - Inside getDevices(): " + devices);
        return devices;
    }

    public static List<Device> getDeviceList() {
        return new ArrayList<>(getDevices().values());
    }

    // 🌟 Add a new device to memory & persist in Excel
    public static void addDevice(Device device) {
        devices.put(device.getId(), device);
        saveDevices();
    }

    // 🌟 Remove a device from memory & Excel
    public static boolean removeDevice(String deviceId) {
        if (devices.containsKey(deviceId)) {
            devices.remove(deviceId);
            saveDevices();
            return true;
        }
        return false;
    }

    // 🌟 Ensure the latest device synchronization before saving to Excel
    public static void saveDevices() {
        System.out.println("🛠️ Saving devices to Excel...");

        try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_NAME);
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Devices");
            String[] headers = {"TYPE", "ID", "NAME", "STATE", "BRAND", "MODEL", "ACTIONS", "ADDED_TS", "UPDATED_TS"};
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (String deviceId : devices.keySet()) {
                Device latestInstance = devices.get(deviceId);  // ✅ Ensure the absolute latest reference

                if (latestInstance == null) {
                    System.out.println("❌ Warning: Device ID " + deviceId + " not found in memory!");
                    continue;
                }

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(latestInstance.getType().name());
                row.createCell(1).setCellValue(latestInstance.getId());
                row.createCell(2).setCellValue(latestInstance.getName());
                row.createCell(3).setCellValue(latestInstance.getState());  // ✅ Prevents outdated state from Excel
                row.createCell(4).setCellValue(Optional.ofNullable(latestInstance.getBrand()).orElse("N/A"));
                row.createCell(5).setCellValue(Optional.ofNullable(latestInstance.getModel()).orElse("N/A"));
                row.createCell(6).setCellValue(String.join(", ", latestInstance.getAvailableActions()));
                row.createCell(7).setCellValue(latestInstance.getAddedTimestamp());
                row.createCell(8).setCellValue(latestInstance.getUpdatedTimestamp());
            }

            workbook.write(fos);
            System.out.println("✅ Devices saved to Excel with correct live states.");
        } catch (IOException e) {
            System.err.println("❌ Failed to save devices: " + e.getMessage());
        }
    }


    // 🌟 Ensure device state updates correctly across memory & persistence
    public static void updateDeviceState(String deviceId, String action) {
        Device device = devices.get(deviceId);

        if (device != null) {
            System.out.println("🔎 Debug - Entering updateDeviceState() for " + deviceId);
            System.out.println("🔍 Debug - Before update: " + device.getId() + " → " + device.getState() + " | Action: " + action);

            boolean shouldTurnOn = action.equalsIgnoreCase(DeviceAction.ON.name());
            System.out.println("🔎 Debug - Should Turn On? " + shouldTurnOn);

            if (shouldTurnOn) {
                device.turnOn();
            } else {
                device.turnOff();
            }

            // 🔄 Explicitly set the state before saving
            device.setState(shouldTurnOn ? DeviceAction.ON.name() : DeviceAction.OFF.name());
            // 🔄 Ensure the latest instance gets stored
            devices.put(deviceId, device);

            saveDevices();

            System.out.println("🔎 Debug - Final verification after setting state: " + device.getId() + " → " + device.getState());
        } else {
            System.out.println("❌ Debug - Device " + deviceId + " not found in storage.");
        }
    }



    // 🌟 Ensures memory and persistence align correctly
    public static void refreshDevices() {
        System.out.println("🔄 Refreshing DeviceStorage...");
        for (String id : devices.keySet()) {
            Device latestInstance = devices.get(id);
            if (latestInstance != null) {
                devices.put(id, latestInstance);
            }
        }
        System.out.println("✅ DeviceStorage refresh completed.");
    }

    public static List<Thread> getDeviceThreads() {
        return deviceThreads;
    }
}
