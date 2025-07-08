package storage;

import devices.Device;

public class ExcelDevicePersistence implements DevicePersistence {
    @Override
    public boolean updateDevice(Device device) {
        return XlCreator.updateDevice(device);
    }

    @Override
    public boolean removeSensorLink(String deviceId) {
        return XlCreator.removeSensorLink(deviceId);
    }
}
