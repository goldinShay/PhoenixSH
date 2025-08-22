package services;

import devices.Device;
import storage.xlc.XlDeviceManager;

public class DeviceAutomationService {

    public static boolean toggleAutomation(Device device) {
        Device fresh = XlDeviceManager.getDeviceById(device.getId());
        boolean newState = !fresh.isAutomationEnabled();
        fresh.setAutomationEnabled(newState);
        return newState;
    }

    public static boolean isAutomationEnabled(String deviceId) {
        Device device = XlDeviceManager.getDeviceById(deviceId);
        return device != null && device.isAutomationEnabled();
    }
}
