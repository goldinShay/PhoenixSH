package ui;

import devices.Device;
import devices.DeviceAction;
import devices.Thermostat;
import scheduler.Scheduler;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import utils.AutoOpManager;
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

            showDeviceControlMenu(selectedDevice, scheduler);
        }
    }

    private static void showDeviceControlMenu(Device device, Scheduler scheduler) {
        while (true) {
            System.out.println("\n=== Device Control Menu ===");
            System.out.println(device.getName() + " is currently " + device.getState());
            System.out.println("1 - Set ON / Force ON (removes conflicting scheduler tasks)");
            System.out.println("2 - Set OFF / Force OFF (removes conflicting scheduler tasks)");
            System.out.println("3 - AutoOp");
            System.out.println("4 - Actions");
            System.out.println("5 - Test Linked Sensor");
            System.out.println("6 - Back");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> setDeviceState(device, scheduler, true);
                case "2" -> setDeviceState(device, scheduler, false);
                case "3" -> {
                    System.out.println("\n=== AutoOp Settings ===");
                    System.out.println("Current AutoOp: " + (device.isAutomationEnabled() ? "🟢 ENABLED" : "🔴 DISABLED"));
                    System.out.println("Linked Sensor: " + (device.getAutomationSensorId() != null ? device.getAutomationSensorId() : "None"));
                    System.out.println("1 - ENABLE AutoOp");
                    System.out.println("2 - DISABLE AutoOp");
                    System.out.println("3 - Back");

                    System.out.print("Choose option: ");
                    String autoChoice = scanner.nextLine().trim();

                    switch (autoChoice) {
                        case "1" -> {
                            System.out.println("\nAvailable Sensors:");
                            SensorStorage.getSensors().forEach((sid, sensor) ->
                                    System.out.printf(" - %s (%s, %s)%n", sid, sensor.getSensorName(), sensor.getUnit()));

                            System.out.print("Enter Sensor ID to assign as Master: ");
                            String masterId = scanner.nextLine().trim();

                            Sensor selectedSensor = SensorStorage.getSensors().get(masterId);

                            if (selectedSensor == null) {
                                System.out.println("❌ Invalid Sensor ID.");
                            } else {
                                // 💡 Always fetch device from DeviceStorage to avoid instance desync
                                Device actualDevice = DeviceStorage.getDevices().get(device.getId());
                                actualDevice.setAutomationEnabled(true);
                                actualDevice.setAutomationSensorId(masterId);

                                if (!selectedSensor.getSlaves().contains(actualDevice)) {
                                    selectedSensor.addSlave(actualDevice);
                                }

                                if (XlCreator.updateDevice(actualDevice)) {
                                    System.out.println("💾 AutoOp status saved to Excel.");
                                } else {
                                    System.out.println("⚠️ Failed to update Excel with AutoOp state.");
                                }

                                Device freshDevice = DeviceStorage.getDevices().get(actualDevice.getId()); // 🧼 Fresh from storage

                                System.out.printf("✅ Linking %s (%s) → AUTO-ON: %.1f | AUTO-OFF: %.1f%n",
                                        freshDevice.getName(),
                                        freshDevice.getId(),
                                        freshDevice.getAutoOnThreshold(),
                                        freshDevice.getAutoOffThreshold());

                                AutoOpManager.persistLink(freshDevice, selectedSensor);

                                selectedSensor.notifySlaves(selectedSensor.getCurrentValue());

                                System.out.println("✅ AutoOp ENABLED for device: " + actualDevice.getName());
                                System.out.println("🔗 Linked to sensor: " + selectedSensor.getSensorName() + " (" + masterId + ")");
                                System.out.printf("📊 Thresholds → Auto-ON: %.0f %s | Auto-OFF: %.0f %s%n",
                                        actualDevice.getAutoOnThreshold(), selectedSensor.getUnit(),
                                        actualDevice.getAutoOffThreshold(), selectedSensor.getUnit());
                            }

                        }

                        case "2" -> {
                            // 🌐 Capture sensor ID before we null it (for cleanup)
                            String sensorId = device.getAutomationSensorId();

                            // 🔕 Disable AutoOp in memory
                            device.setAutomationEnabled(false);
                            device.setAutomationSensorId(null);

                            // 💾 Save changes to Excel
                            if (XlCreator.updateDevice(device)) {
                                System.out.println("💾 AutoOp DISABLED and saved to Excel.");

                                // 🧹 Remove from sensor’s in-memory slave list
                                Sensor sensor = SensorStorage.getSensors().get(sensorId);
                                if (sensor != null) {
                                    sensor.getSlaves().remove(device);
                                    System.out.printf("🚪 Removed '%s' from sensor '%s' slave list%n",
                                            device.getId(), sensor.getSensorId());
                                }

                                // 📄 Remove from Excel’s Sense_Control sheet
                                if (XlCreator.removeSensorLink(device.getId())) {
                                    System.out.println("🧻 Device mapping removed from Sense_Control sheet.");
                                } else {
                                    System.out.println("⚠️ Failed to remove mapping from Sense_Control.");
                                }

                            } else {
                                System.out.println("⚠️ Failed to save AutoOp disablement.");
                            }

                            System.out.println("✅ AutoOp DISABLED for device: " + device.getName());
                        }


                        case "3" -> System.out.println("↩️ Back to device menu.");
                        default -> System.out.println("❌ Invalid choice.");
                    }
                }


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
                                case "4" -> { return; }
                                default -> System.out.println("❌ Invalid option. Please choose 1-4.");
                            }
                        }
                    } else {
                        System.out.println("⚠️ Actions only available for Thermostat devices.");
                    }
                }

                case "5" -> {
                    if (!device.isAutomationEnabled()) {
                        System.out.println("⚠️ Automation is not enabled. No sensor linked.");
                        break;
                    }

                    String sensorId = device.getAutomationSensorId();
                    Sensor sensor = SensorStorage.getSensors().get(sensorId);

                    if (sensor == null) {
                        System.out.println("❌ Linked sensor not found: " + sensorId);
                    } else {
                        System.out.println("\n🧪 Testing Linked Sensor: " + sensor.getSensorName() + " (" + sensorId + ")");
                        sensor.testSensorBehavior();
                    }
                }

                case "6" -> {
                    System.out.println("↩️ Returning to Monitor Menu...");
                    return;
                }




                default -> System.out.println("❌ Invalid option. Please choose 1-6.");
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
