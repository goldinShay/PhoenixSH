package ui.gui.utils;

import devices.Device;
import devices.actions.LiveDeviceState;

import java.awt.*;

public class DeviceStatusUtils {

    public static String getLiveStatusText(Device device) {
        return LiveDeviceState.isOn(device) ? "🟢 ON" : "🔴 OFF";
    }

    public static Color getLiveStatusColor(Device device) {
        return LiveDeviceState.isOn(device) ? Color.GREEN : Color.RED;
    }
}
