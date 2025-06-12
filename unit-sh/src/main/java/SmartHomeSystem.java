import scheduler.Scheduler;
import storage.DeviceStorage;
import ui.Menu;
import utils.DeviceIdManager;

import java.util.ArrayList;

public class SmartHomeSystem {
    public static void main(String[] args) {
        System.out.println("📂 Initializing Smart Home System...");

        // ✅ Load devices FIRST
        initializeDevices();

        // ✅ Only initialize the Scheduler AFTER devices exist in memory
        Scheduler scheduler = new Scheduler(DeviceStorage.getDevices()); // ✅ Ensure Scheduler has access to devices
        System.out.println("Stored devices before loading tasks: " + DeviceStorage.getDeviceList());
        scheduler.loadTasksFromExcel();
        scheduler.startSchedulerLoop();


        // 🚀 Launch main menu
        Menu.show(DeviceStorage.getDevices(), DeviceStorage.getDeviceThreads(), scheduler);
    }

    private static void initializeDevices() {
        System.out.println("📂 Loading devices from Excel...");
        DeviceStorage.initialize(); // ✅ Ensures devices are properly stored

        // 🔄 Sync DeviceIdManager AFTER devices are registered
        DeviceIdManager.getInstance().setExistingDevices(new ArrayList<>(DeviceStorage.getDevices().values()));

        int deviceCount = DeviceStorage.getDevices().size();
        System.out.println("✅ System initialized with " + deviceCount + " device" + (deviceCount == 1 ? "" : "s") + ".");
    }
}
