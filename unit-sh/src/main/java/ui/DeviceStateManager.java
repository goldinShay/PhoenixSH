package ui;

import devices.Device;
import devices.actions.DeviceAction;
import scheduler.Scheduler;
import storage.DeviceStorage;
import utils.EmailService;

public class DeviceStateManager {

    public static void updateState(Device device, Scheduler scheduler, boolean turnOn) {
        String action = resolveAction(turnOn);

        removeConflictingTasks(device, scheduler, action);
        applyStateChange(device, action);
        notifyUser(device, action);

        System.out.println("✅ " + device.getName() + " is now " + action);
    }

    private static String resolveAction(boolean turnOn) {
        return turnOn ? DeviceAction.ON.name() : DeviceAction.OFF.name();
    }

    private static void removeConflictingTasks(Device device, Scheduler scheduler, String action) {
        scheduler.removeTaskIfConflicts(device.getId(), action);
    }

    private static void applyStateChange(Device device, String action) {
        device.setState(action);
        DeviceStorage.updateDeviceState(device.getId(), action);
    }

    private static void notifyUser(Device device, String action) {
        EmailService.sendDeviceActionEmail(
                "javagoldin@gmail.com",
                device.getType().name(),
                device.getId(),
                device.getName(),
                action
        );
    }
}
