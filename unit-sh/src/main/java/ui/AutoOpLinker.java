package ui;

import devices.Device;
import sensors.Sensor;
import storage.SensorStorage;
import utils.AutoOpManager;

import java.util.Scanner;
import java.util.Map;


public class AutoOpLinker {
    private static final Scanner defaultScanner = new Scanner(System.in);

    public static void enable(Device device) {
        enable(device, defaultScanner);
    }

    // 👉 Add this method for testing
    public static void enable(Device device, Scanner scanner) {
        System.out.println("\n📡 Available Sensors:");

        if (SensorStorage.getSensors().isEmpty()) {
            System.out.println("⚠️ No sensors available.");
            return;
        }

        for (Map.Entry<String, Sensor> entry : SensorStorage.getSensors().entrySet()) {
            Sensor s = entry.getValue();
            try {
                int reading = s.getCurrentReading();
                System.out.printf("→ %s | %s | Current: %d.0%n", s.getSensorId(), s.getSensorName(), reading);
            } catch (Exception e) {
                System.out.printf("→ %s | %s | Current: ⚠️ Invalid reading (%s)%n", s.getSensorId(), s.getSensorName(), e.getMessage());
            }
        }

        System.out.print("Enter sensor ID to link with device '" + device.getName() + "': ");
        String sensorId = scanner.nextLine().trim();

        Sensor selected = SensorStorage.getSensors().get(sensorId);
        if (selected == null) {
            System.out.println("❌ Sensor not found. AutoOp not enabled.");
            return;
        }

        selected.addSlave(device);
        device.setAutomationSensorId(sensorId);
        device.setAutomationEnabled(true);
        device.enableAutoMode();

        boolean persisted = AutoOpManager.persistLink(device, selected);
        if (persisted) {
            System.out.printf("🔗 '%s' successfully linked to sensor '%s'.%n", device.getName(), selected.getSensorName());
        }
    }
}
