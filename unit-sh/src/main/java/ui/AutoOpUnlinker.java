package ui;

import devices.Device;
import sensors.Sensor;
import storage.DevicePersistence;
import storage.ExcelDevicePersistence;
import storage.SensorStorage;

public class AutoOpUnlinker {

    private static DevicePersistence persistence = new ExcelDevicePersistence();

    public static void setPersistence(DevicePersistence customPersistence) {
        persistence = customPersistence;
    }

    public static void disable(Device device) {
        String sensorId = device.getAutomationSensorId();

        device.setAutomationEnabled(false);
        device.setAutomationSensorId(null);
        device.setLinkedSensor(null);
        device.disableAutoMode();

        if (!persistence.updateDevice(device)) {
            System.out.println("⚠️ Failed to save AutoOp disablement.");
            return;
        }

        System.out.println("💾 AutoOp DISABLED and saved to Excel.");

        Sensor sensor = SensorStorage.getSensors().get(sensorId);
        if (sensor != null) {
            sensor.getSlaves().remove(device);
            System.out.printf("🚪 Removed '%s' from sensor '%s' slave list%n",
                    device.getId(), sensor.getSensorId());
        }

        if (persistence.removeSensorLink(device.getId())) {
            System.out.println("🧻 Device mapping removed from Sense_Control sheet.");
        } else {
            System.out.println("⚠️ Failed to remove mapping from Sense_Control.");
        }

        System.out.println("✅ AutoOp DISABLED for device: " + device.getName());
    }
}
