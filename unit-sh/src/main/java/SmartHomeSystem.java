import devices.Device;
import devices.actions.LiveDeviceState;
import scheduler.Scheduler;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import storage.xlc.XlTaskSchedulerManager;
import storage.xlc.XlWorkbookUtils;
import ui.Menu;
import ui.gui.MainWindow;
import autoOp.AutoOpManager;
import ui.gui.PageNavigator;
import ui.gui.managers.ButtonMapManager;
import ui.gui.managers.GuiStateManager;
import utils.DeviceIdManager;
import devices.DeviceType;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

import storage.xlc.XlAutoOpManager;

import javax.swing.*;

import static ui.gui.managers.ButtonMapManager.renderPageForTypes;

public class SmartHomeSystem {

    private static Scheduler scheduler;

    public static void main(String[] args) {
        boolean guiMode = args.length > 0 && args[0].equalsIgnoreCase("gui");

        System.out.println("📂 Initializing Smart Home System...");

        if (!validateWorkbook(guiMode)) {
            System.err.println("🚫 Startup aborted due to missing or corrupt Excel file.");
            return;
        }

        if (guiMode) {
            MainWindow.initialize();     // ✅ Construct GUI first
            initializeSystem();          // ✅ Now safe to call PageNavigator
            MainWindow.launch();              // ✅ Show GUI
            new Thread(SmartHomeSystem::launchCli).start();
        } else {
            initializeSystem();      // CLI-only mode
            launchCli();
        }
    }


    private static boolean validateWorkbook(boolean guiMode) {
        File excelFile = XlWorkbookUtils.getFilePath().toFile();

        if (!excelFile.exists() || !XlWorkbookUtils.isExcelFileHealthy(excelFile)) {
            if (guiMode) {
                return XlWorkbookUtils.ensureFileExists(); // GUI prompt
            } else {
                return ensureExcelFileExists(false); // CLI prompt
            }
        }

        return true;
    }

    private static void initializeSystem() {
        try {
            GuiStateManager.refreshGuiFromMemory();

            DeviceStorage.initialize();              // ✅ Devices loaded here
            SensorStorage.loadSensorsFromExcel();
            XlTaskSchedulerManager.loadTasks();

            linkDevicesAndSensors();
            prepareScheduler();

            GuiStateManager.refreshDeviceMatrix();   // ✅ Buttons registered here

            // ✅ NOW the matrix is ready — build and register page 120
            JPanel lightPage = ButtonMapManager.renderPageForTypes(
                    new DeviceType[]{DeviceType.LIGHT}, 0, 120
            );
            PageNavigator.registerPage(120, lightPage);

            System.out.println("✅ System initialized successfully.");
        } catch (Exception e) {
            System.err.println("🚨 System initialization failed: " + e.getMessage());
            e.printStackTrace();
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

    private static void linkDevicesAndSensors() {
//        AutoOpLinker.relinkLinkedDevicesToSensors();
        AutoOpManager.reevaluateAllSensors();
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
}
