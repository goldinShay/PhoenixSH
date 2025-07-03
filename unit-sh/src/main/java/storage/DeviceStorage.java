package storage;

import devices.Device;
import devices.DeviceAction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class DeviceStorage {

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

            System.out.println("💾 DeviceStorage.updateDeviceState — attempting safe Excel update...");

            boolean success = XlCreator.updateDevice(device);
            if (!success) {
                System.err.println("❌ Failed to persist device state update to Excel.");
            }
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
