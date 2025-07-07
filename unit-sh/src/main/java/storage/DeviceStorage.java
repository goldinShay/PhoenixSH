package storage;

import devices.Device;
import devices.actions.DeviceAction;

import java.util.*;

public class DeviceStorage {

    // 🔧 Static storage for all devices
    private static final Map<String, Device> devices = new HashMap<>();

    // 🔧 Track active threads (if any)
    private static final List<Thread> deviceThreads = new ArrayList<>();

    // 🧪 Clear in-memory state for clean test execution
    public static void clear() {
        devices.clear();
        deviceThreads.clear();
    }

    // 🔄 Initialize by loading from Excel (usually called on startup)
    public static void initialize() {
        devices.clear();
        List<Device> loadedDevices = XlCreator.loadDevicesFromExcel();
        loadedDevices.forEach(device -> devices.put(device.getId(), device));
    }

    // 🚪 Expose all devices, auto-synced before returning
    public static Map<String, Device> getDevices() {
        refreshDevices();
        return devices;
    }

    // 📄 Alternate form: get devices as a list
    public static List<Device> getDeviceList() {
        return new ArrayList<>(getDevices().values());
    }

    // 🔄 Used for memory vs persistence sanity checks (currently a no-op but might evolve)
    public static void refreshDevices() {
        devices.replaceAll((id, latestInstance) -> latestInstance);
    }

    // ⚡ Turn a device on/off + update Excel
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

    // 🧵 Thread tracking access
    public static List<Thread> getDeviceThreads() {
        return deviceThreads;
    }
}
