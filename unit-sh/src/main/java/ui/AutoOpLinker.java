package ui;

import devices.Device;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import utils.AutoOpManager;

import java.util.Scanner;

public class AutoOpLinker {
    private static final Scanner scanner = new Scanner(System.in);

    public static void enable(Device device) {
        System.out.println("\nAvailable Sensors:");
        SensorStorage.getSensors().forEach((sid, sensor) ->
                System.out.printf(" - %s (%s, %s)%n", sid, sensor.getSensorName(), sensor.getUnit()));

        System.out.print("Enter Sensor ID to assign as Master: ");
        String sensorId = scanner.nextLine().trim();

        Sensor sensor = SensorStorage.getSensors().get(sensorId);

        if (sensor == null) {
            System.out.println("❌ Invalid Sensor ID.");
            return;
        }

        Device freshDevice = DeviceStorage.getDevices().get(device.getId());
        freshDevice.setAutomationEnabled(true);
        freshDevice.setAutomationSensorId(sensorId);

        if (!sensor.getSlaves().contains(freshDevice)) {
            sensor.addSlave(freshDevice);
        }

        if (XlCreator.updateDevice(freshDevice)) {
            System.out.println("💾 AutoOp status saved to Excel.");
        } else {
            System.out.println("⚠️ Failed to update Excel with AutoOp state.");
        }

        System.out.printf("✅ Linking %s (%s) → AUTO-ON: %.1f | AUTO-OFF: %.1f%n",
                freshDevice.getName(),
                freshDevice.getId(),
                freshDevice.getAutoOnThreshold(),
                freshDevice.getAutoOffThreshold());

        AutoOpManager.persistLink(freshDevice, sensor);
        sensor.notifySlaves(sensor.getCurrentValue());

        System.out.println("✅ AutoOp ENABLED for device: " + freshDevice.getName());
        System.out.println("🔗 Linked to sensor: " + sensor.getSensorName() + " (" + sensorId + ")");
        System.out.printf("📊 Thresholds → Auto-ON: %.0f %s | Auto-OFF: %.0f %s%n",
                freshDevice.getAutoOnThreshold(), sensor.getUnit(),
                freshDevice.getAutoOffThreshold(), sensor.getUnit());
    }
}
