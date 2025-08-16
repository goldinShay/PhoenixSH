package storage;

import devices.Device;
import utils.Log;

public class ExcelDevicePersistence implements DevicePersistence {

    private static boolean systemInitializing = true;

    public static void setInitFlag(boolean value) {
        systemInitializing = value;
    }

    @Override
    public boolean updateDevice(Device device) {
        if (systemInitializing) {
            Log.debug("🛡️ Skipping Excel write during system initialization.");
            return true;
        }

        return XlCreator.delegateDeviceUpdate(device); // Already wrapped internally
    }

    @Override
    public boolean removeSensorLink(String deviceId) {
        XlCreator.removeSensorLink(deviceId); // void method
        return true; // fudge a success return value
    }
}