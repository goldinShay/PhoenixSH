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

    // Initialize storage by loading devices from Excel
    public static void initialize() {
        devices.clear();
        List<Device> loadedDevices = XlCreator.loadDevicesFromExcel();
        loadedDevices.forEach(device -> devices.put(device.getId(), device));
    }

    // Get all stored devices with real-time synchronization
    public static Map<String, Device> getDevices() {
        refreshDevices();
        return devices;
    }

    public static List<Device> getDeviceList() {
        return new ArrayList<>(getDevices().values());
    }

    // Add a new device to memory & persist in Excel
    public static void addDevice(Device device) {
        devices.put(device.getId(), device);
        saveDevices();
    }

    // Remove a device from memory & Excel
    public static boolean removeDevice(String deviceId) {
        if (devices.remove(deviceId) != null) {
            saveDevices();
            return true;
        }
        return false;
    }

    // Ensure the latest device synchronization before saving to Excel
    public static void saveDevices() {
        try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_NAME);
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Devices");
            String[] headers = {"TYPE", "ID", "NAME", "STATE", "BRAND", "MODEL", "ACTIONS", "ADDED_TS", "UPDATED_TS"};
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (Device device : devices.values()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(device.getType().name());
                row.createCell(1).setCellValue(device.getId());
                row.createCell(2).setCellValue(device.getName());
                row.createCell(3).setCellValue(device.getState());
                row.createCell(4).setCellValue(Optional.ofNullable(device.getBrand()).orElse("N/A"));
                row.createCell(5).setCellValue(Optional.ofNullable(device.getModel()).orElse("N/A"));
                row.createCell(6).setCellValue(String.join(", ", device.getAvailableActions()));
                row.createCell(7).setCellValue(device.getAddedTimestamp());
                row.createCell(8).setCellValue(device.getUpdatedTimestamp());
            }

            workbook.write(fos);
        } catch (IOException e) {
            System.err.println("❌ Failed to save devices: " + e.getMessage());
        }
    }

    // Ensure device state updates correctly across memory & persistence
    public static void updateDeviceState(String deviceId, String action) {
        Device device = devices.get(deviceId);
        if (device != null) {
            boolean shouldTurnOn = action.equalsIgnoreCase(DeviceAction.ON.name());
            if (shouldTurnOn) {
                device.turnOn();
            } else {
                device.turnOff();
            }

            device.setState(shouldTurnOn ? DeviceAction.ON.name() : DeviceAction.OFF.name());
            devices.put(deviceId, device);
            saveDevices();
        }
    }

    // Ensures memory and persistence align correctly
    public static void refreshDevices() {
        devices.replaceAll((id, latestInstance) -> latestInstance);
    }

    public static List<Thread> getDeviceThreads() {
        return deviceThreads;
    }
}
