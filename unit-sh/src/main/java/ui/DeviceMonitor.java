package ui;

import devices.Device;
import devices.actions.DeviceAction;
import scheduler.Scheduler;
import storage.DeviceStorage;
import utils.EmailService;

import java.util.Map;
import java.util.Scanner;

public class DeviceMonitor {

    private static final Scanner scanner = new Scanner(System.in);

    public static void showMonitorDeviceMenu(Map<String, Device> devices, Scheduler scheduler) {
        while (true) {
            System.out.println("\n=== Monitor Device Menu ===");
            System.out.println("📍 Select a device to monitor (0 = Back):");

            int index = 1;
            for (Device device : devices.values()) {
                System.out.println(index++ + " - " + device.getType() + " | " + device.getName() + " | " + device.getId());
            }

            System.out.print("Enter device ID or 0 to go back: ");
            String deviceId = scanner.nextLine().trim();

            if (deviceId.equals("0")) break;

            Device selectedDevice = DeviceStorage.getDevices().get(deviceId);
            if (selectedDevice == null) {
                System.out.println("❌ Invalid ID. Try again.");
                continue;
            }

// 🔀 New: Delegate to correct action menu
            routeToActionMenu(selectedDevice);
        }
    }
    private static void routeToActionMenu(Device device) {
        switch (device.getType()) {
            case LIGHT -> ui.deviceActionMenu.LightActionsMenu.show(device);
            case THERMOSTAT -> ui.deviceActionMenu.ThermostatActionsMenu.show(device);
            case WASHING_MACHINE -> ui.deviceActionMenu.WasherActionsMenu.show(device);
            case DRYER -> ui.deviceActionMenu.DryerActionsMenu.show(device);
            case SMART_LIGHT -> ui.deviceActionMenu.SmartLightActionsMenu.show(device);
            default -> System.out.println("ℹ️ No specialized actions available for this device.");
        }
    }


}
