import storage.DeviceStorage;
import ui.Menu;
import utils.DeviceIdManager;

import java.util.ArrayList;

public class SmartHomeSystem {

    public static void main(String[] args) {
        initializeDevices();  // ✅ Ensures devices are stored properly

        // 🚪 Open the menu with persistent storage access
        Menu.show(DeviceStorage.getDevices(), DeviceStorage.getDeviceThreads());
    }

    public static void initializeDevices() {
        System.out.println("📂 Loading devices from Excel...");
        DeviceStorage.initialize(); // ✅ Uses storage instead of local map

        // 🧠 Sync DeviceIdManager with existing IDs
        DeviceIdManager.getInstance().setExistingDevices(new ArrayList<>(DeviceStorage.getDevices().values()));

        System.out.println("✅ Initialized system with " + DeviceStorage.getDevices().size() + " devices.");
    }
}
