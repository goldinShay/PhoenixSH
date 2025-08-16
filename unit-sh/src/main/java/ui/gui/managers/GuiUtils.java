package ui.gui.managers;

import devices.Device;
import devices.actions.LiveDeviceState;
import storage.DeviceStorage;
import storage.xlc.XlDeviceManager;

import java.io.IOException;

public class GuiUtils {
    public static void syncGuiAfterDeviceUpdate(Device device) {
        try {
            XlDeviceManager.updateDevice(device);
            DeviceStorage.reloadFromExcel();

            // 🔁 Sync live state
            DeviceStorage.getDevices().values().forEach(d -> {
                if (d.isOn()) {
                    LiveDeviceState.turnOn(d);
                } else {
                    LiveDeviceState.turnOff(d);
                }
            });

            GuiStateManager.refreshDeviceMatrix();
            GuiStateManager.refreshDeviceControlPage(device);
        } catch (IOException ex) {
            System.err.println("❌ Failed to sync GUI: " + ex.getMessage());
        }
    }
    public static void refreshGuiAfterDeviceUpdate(Device device) {
        GuiStateManager.refreshDeviceControlPage(device);
    }
}