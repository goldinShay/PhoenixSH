package storage;

import devices.Device;

public interface DevicePersistence {
    boolean updateDevice(Device device);
    boolean removeSensorLink(String deviceId);
}
