import devices.Device;
import scheduler.Scheduler;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import ui.Menu;
import utils.AutoOpManager;
import utils.Log;

import java.io.File;
import java.io.IOException;

public class SmartHomeSystem {
    public static void main(String[] args) {
        System.out.println("📂 Initializing Smart Home System...");

        File excelFile = XlCreator.getFilePath().toFile();

        // 🛡️ Ensure the Excel file exists or offer to create a new one
        if (!excelFile.exists()) {
            System.out.printf("📄 Excel file not found: %s%n", excelFile.getAbsolutePath());
            System.out.print("🆕 Create a new Excel file? (Y/N): ");

            try {
                char input = (char) System.in.read();
                if (Character.toUpperCase(input) == 'Y') {
                    if (XlCreator.createNewWorkbook()) {
                        System.out.println("✨ New Excel file created successfully.");
                    } else {
                        System.out.println("❌ Failed to create Excel file. Exiting.");
                        return;
                    }
                } else {
                    System.out.println("🚫 Startup aborted by user.");
                    return;
                }
            } catch (IOException e) {
                System.out.println("⚠️ Error reading user input: " + e.getMessage());
                return;
            }
        }

        // 1️⃣ Load core memory from Excel
        DeviceStorage.initialize();                     // Devices with correct AutoOp flags
        SensorStorage.loadSensorsFromExcel();          // Sensors
        XlCreator.loadSensorLinksFromExcel();          // Optional crosslinker

        // 2️⃣ Restore automation links (Sensor ↔ Device memory references)
        AutoOpManager.restoreMemoryLinks();            // Sets automationEnabled + links slaves
        relinkSlavesToSensors();                       // Safe to associate sensors now

        // 3️⃣ Trigger threshold logic
        AutoOpManager.reevaluateAllSensors();          // Notify slaves if needed

        // 4️⃣ Start scheduler
        Scheduler scheduler = new Scheduler(
                DeviceStorage.getDevices(),
                SensorStorage.getSensors()
        );
        System.out.println("Stored devices before loading tasks: " + DeviceStorage.getDeviceList());
        scheduler.loadTasksFromExcel();

        new java.util.Timer(true).schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                scheduler.startSchedulerLoop();
            }
        }, 3000); // Delay for graceful boot

        // 🚀 System menu interaction
        Menu.show(DeviceStorage.getDevices(), DeviceStorage.getDeviceThreads(), scheduler);
    }


    // ✅ Clean slave re-link logic (now that automation flags are confirmed)
    private static void relinkSlavesToSensors() {
        // 🧼 Clear all sensor slave links before relinking
        for (Sensor sensor : SensorStorage.getSensors().values()) {
            sensor.getSlaves().clear();
            System.out.printf("🧹 Cleared slave list for sensor '%s'%n", sensor.getSensorId());
        }

        // 🔁 Rewire slave links based on active device mappings
        for (Device device : DeviceStorage.getDevices().values()) {
            if (!device.isAutomationEnabled()) continue;

            String sensorId = device.getAutomationSensorId();
            Sensor sensor = SensorStorage.getSensors().get(sensorId);

            if (sensor != null) {
                System.out.printf("💡 Before link, sensor '%s' has %d slaves%n",
                        sensor.getSensorId(), sensor.getSlaves().size());

                sensor.addSlave(device);

                System.out.printf("🔗 Final Link → %s → %s | AutoOp: %b | Ref: %s%n",
                        device.getId(), sensor.getSensorId(), device.isAutomationEnabled(),
                        System.identityHashCode(device));
            } else {
                Log.warn("⚠️ No sensor found for device " + device.getId() + " (Sensor ID: " + sensorId + ")");
            }
        }
    }
}
