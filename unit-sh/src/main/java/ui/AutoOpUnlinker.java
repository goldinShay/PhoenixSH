package ui;

import devices.Device;
import sensors.Sensor;
import storage.SensorStorage;
import storage.XlCreator;

public class AutoOpUnlinker {

    public static void disable(Device device) {
        String sensorId = device.getAutomationSensorId();

        // 📴 Update device's automation state
        device.setAutomationEnabled(false);
        device.setAutomationSensorId(null);

        boolean success = XlCreator.updateDevice(device);
        if (!success) {
            System.out.println("⚠️ Failed to save AutoOp disablement.");
            return;
        }

        System.out.println("💾 AutoOp DISABLED and saved to Excel.");

        // 👋 Remove from linked sensor's slave list
        Sensor sensor = SensorStorage.getSensors().get(sensorId);
        if (sensor != null) {
            sensor.getSlaves().remove(device);
            System.out.printf("🚪 Removed '%s' from sensor '%s' slave list%n",
                    device.getId(), sensor.getSensorId());
        }

        // 🧹 Remove Excel Sense_Control mapping
        if (XlCreator.removeSensorLink(device.getId())) {
            System.out.println("🧻 Device mapping removed from Sense_Control sheet.");
        } else {
            System.out.println("⚠️ Failed to remove mapping from Sense_Control.");
        }

        System.out.println("✅ AutoOp DISABLED for device: " + device.getName());
    }
}
