package devices.actions;

import devices.Device;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LiveDeviceState {
    private static final Set<String> activeDeviceIds = new HashSet<>();

    public static void turnOn(Device device) {
        activeDeviceIds.add(device.getId());
    }

    public static void turnOff(Device device) {
        activeDeviceIds.remove(device.getId());
    }

    public static boolean isOn(Device device) {
        return activeDeviceIds.contains(device.getId());
    }

    public static Set<String> getActiveDevices() {
        return Collections.unmodifiableSet(activeDeviceIds);
    }
}

