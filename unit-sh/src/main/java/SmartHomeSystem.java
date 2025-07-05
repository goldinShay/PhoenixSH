import devices.Device;
import scheduler.Scheduler;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import storage.xlc.XlWorkbookUtils;
import ui.Menu;
import ui.gui.MainWindow;
import utils.AutoOpManager;
import utils.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SmartHomeSystem {

    private static Scheduler scheduler;

    public static void main(String[] args) {
        boolean guiMode = args.length > 0 && args[0].equalsIgnoreCase("gui");

        System.out.println("📂 Initializing Smart Home System...");

        if (!ensureExcelFileExists(guiMode)) {
            return; // Startup aborted or failed
        }

        initializeSystem();

        if (guiMode) {
            launchGui();
            new Thread(SmartHomeSystem::launchCli).start(); // CLI runs in parallel
        } else {
            launchCli();
        }

    }


    private static boolean ensureExcelFileExists(boolean guiMode) {
        File excelFile = XlWorkbookUtils.getFilePath().toFile();

        if (excelFile.exists()) return true;

        System.out.printf("📄 Excel file not found: %s%n", excelFile.getAbsolutePath());

        if (guiMode) {
            System.err.println("🚫 No Excel file found. GUI mode requires one to continue.");
            return false;
        }

        System.out.print("🆕 Create a new Excel file? (Y/N): ");
        try {
            char input = (char) System.in.read();
            if (Character.toUpperCase(input) == 'Y') {
                if (XlCreator.createNewWorkbook()) {
                    System.out.println("✨ New Excel file created successfully.");
                    return true;
                } else {
                    System.out.println("❌ Failed to create Excel file. Exiting.");
                }
            } else {
                System.out.println("🚫 Startup aborted by user.");
            }
        } catch (IOException e) {
            System.out.println("⚠️ Error reading user input: " + e.getMessage());
        }

        return false;
    }

    private static void initializeSystem() {
        try {
            initializeDataStores();
            linkDevicesAndSensors();
            prepareScheduler();
        } catch (Exception e) {
            System.err.println("🚨 System initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDataStores() {
        DeviceStorage.initialize();
        SensorStorage.loadSensorsFromExcel();

        Map<String, Device> devices = DeviceStorage.getDevices();
        Map<String, Sensor> sensors = SensorStorage.getSensors();
        XlCreator.loadSensorLinks(devices, sensors);

        AutoOpManager.restoreMemoryLinks(); // Load persisted sensor-device connections
    }

    private static void linkDevicesAndSensors() {
        relinkSlavesToSensors();
        AutoOpManager.reevaluateAllSensors(); // Immediately apply automation logic after linkage
    }

    private static void prepareScheduler() {
        scheduler = new Scheduler(DeviceStorage.getDevices(), SensorStorage.getSensors());
        scheduler.loadTasksFromExcel();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scheduler.startSchedulerLoop();
            }
        }, 3000);

        System.out.println("✅ Scheduler initialized and will start in 3 seconds.");
    }


    private static void launchGui() {
        System.out.println("🖥️ Launching PhoenixSH GUI...");
        MainWindow.launch();
    }

    private static void launchCli() {
        Menu.show(DeviceStorage.getDevices(), DeviceStorage.getDeviceThreads(), scheduler);
    }

    private static void relinkSlavesToSensors() {
        for (Sensor sensor : SensorStorage.getSensors().values()) {
            sensor.getSlaves().clear();
            System.out.printf("🧹 Cleared slave list for sensor '%s'%n", sensor.getSensorId());
        }

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
