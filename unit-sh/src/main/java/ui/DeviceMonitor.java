package ui;

import devices.Device;
import devices.DeviceAction;
import devices.Thermostat;
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

            Device selectedDevice = devices.get(deviceId);
            if (selectedDevice == null) {
                System.out.println("❌ Invalid ID. Try again.");
                continue;
            }

            showDeviceControlMenu(selectedDevice, scheduler);
        }
    }

    private static void showDeviceControlMenu(Device device, Scheduler scheduler) {
        while (true) {
            System.out.println("\n=== Device Control Menu ===");
            System.out.println(device.getName() + " is currently " + device.getState());
            System.out.println("1 - Set ON / Force ON (removes conflicting scheduler tasks)");
            System.out.println("2 - Set OFF / Force OFF (removes conflicting scheduler tasks)");
            System.out.println("3 - AutoOp (coming soon)");
            System.out.println("4 - Actions");
            System.out.println("5 - Back");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> setDeviceState(device, scheduler, true);
                case "2" -> setDeviceState(device, scheduler, false);
                case "3" -> System.out.println("🔧 AutoOp feature coming soon...");
                case "4" -> {
                    if (device instanceof Thermostat thermostat) {
                        while (true) {
                            System.out.println("\n=== Thermostat Actions ===");
                            System.out.println("Current User Temp: " + thermostat.getUserTemp() + "°C");
                            System.out.println("1 - Set Default Temp (25°C)");
                            System.out.println("2 - Increase Temp (+1°C)");
                            System.out.println("3 - Decrease Temp (-1°C)");
                            System.out.println("4 - Back");

                            System.out.print("Choose an option: ");
                            String actionInput = scanner.nextLine().trim();

                            switch (actionInput) {
                                case "1" -> {
                                    thermostat.setUserTemp(25.0);
                                    System.out.println("🌡️ User temp reset to 25°C.");
                                }
                                case "2" -> {
                                    thermostat.increaseUserTemp();
                                    System.out.println("🌡️ User temp increased to " + thermostat.getUserTemp() + "°C.");
                                }
                                case "3" -> {
                                    thermostat.decreaseUserTemp();
                                    System.out.println("🌡️ User temp decreased to " + thermostat.getUserTemp() + "°C.");
                                }
                                case "4" -> { return; } // ✅ Back to device control menu
                                default -> System.out.println("❌ Invalid option. Please choose 1-4.");
                            }
                        }
                    } else {
                        System.out.println("⚠️ Actions only available for Thermostat devices.");
                    }
                }

                case "5" -> { return; } // ✅ Returns to the monitor menu
                default -> System.out.println("❌ Invalid option. Please choose 1-5.");
            }
        }
    }

    private static void setDeviceState(Device device, Scheduler scheduler, boolean turnOn) {
        String action = turnOn ? DeviceAction.ON.name() : DeviceAction.OFF.name();

        // 🔄 Remove conflicting scheduled tasks
        scheduler.removeTaskIfConflicts(device.getId(), action);

        // 🔄 Update device state
        device.setState(action);
        DeviceStorage.updateDeviceState(device.getId(), action);

        // 📧 Send email notification
        EmailService.sendDeviceActionEmail("javagoldin@gmail.com", device.getType().name(), device.getId(), device.getName(), action);

        System.out.println("✅ " + device.getName() + " is now " + action);
    }

}
