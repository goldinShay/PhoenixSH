package ui;


import devices.Device;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;

import java.util.List;
import java.util.Map;

import static ui.Menu.scanner;

public class DeviceMenu {
    public static void DevicesMenu(Map<String, Device> devices, List<Thread> deviceThreads) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Devices Menu ===");
            System.out.println("1 - List Devices & Sensors");
            System.out.println("2 - Embed  Device or Sensor");
            System.out.println("3 - Update Device or Sensor");
            System.out.println("4 - Remove Device or Sensor");
            System.out.println("5 - Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> DeviceViewer.displayAllDevicesAndSensors();
                case "2" -> AddDeviceMenu.addDeviceMenu(devices, deviceThreads);

                case "3" -> {
                    DeviceViewer.displayAllDevicesAndSensors();
                    System.out.print("Enter ID of the device or sensor to update: ");
                    String updateId = scanner.nextLine().trim();

                    // 🔍 Try device
                    if (DeviceStorage.getDevices().containsKey(updateId)) {
                        Device device = DeviceStorage.getDevices().get(updateId);

                        System.out.print("Enter new name (current: " + device.getName() + "): ");
                        String newName = scanner.nextLine().trim();

                        System.out.print("Enter new brand (current: " + device.getBrand() + "): ");
                        String newBrand = scanner.nextLine().trim();

                        System.out.print("Enter new model (current: " + device.getModel() + "): ");
                        String newModel = scanner.nextLine().trim();

                        System.out.print("Enter new Auto-ON threshold (current: " + device.getAutoOnThreshold() + ") or leave blank to keep: ");
                        String newOnStr = scanner.nextLine().trim();

                        // 🧼 Apply updates
                        device.setName(!newName.isEmpty() ? newName : device.getName());
                        device.setBrand(!newBrand.isEmpty() ? newBrand : device.getBrand());
                        device.setModel(!newModel.isEmpty() ? newModel : device.getModel());

                        boolean thresholdChanged = false;

                        try {
                            if (!newOnStr.isEmpty()) {
                                if (newOnStr.equalsIgnoreCase("reset") || newOnStr.equalsIgnoreCase("r")) {
                                    device.resetAutoOnThreshold();
                                    device.resetAutoOffThreshold(); // Mirror reset
                                } else {
                                    double newVal = Double.parseDouble(newOnStr);
                                    device.setAutoOnThreshold(newVal, true);
                                    device.setAutoOffThreshold(newVal); // Mirror ON → OFF
                                }
                                thresholdChanged = true;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("⚠️ Invalid threshold. Keeping previous value.");
                        }

                        device.updateTimestamp();
                        boolean updated = XlCreator.updateDevice(device);
                        System.out.println(updated ? "✅ Device updated." : "❌ Update failed.");

                        if (thresholdChanged && device.getAutomationSensorId() != null) {
                            boolean synced = XlCreator.updateAutoOpThresholds(
                                    device.getId(),
                                    device.getAutoOnThreshold(),
                                    device.getAutoOnThreshold() // Mirror into Excel AUTO-OFF
                            );
                            if (synced) {
                                System.out.println("🔄 Sense_Control threshold synced (AUTO-OFF mirrored).");
                            } else {
                                System.out.println("⚠️ Failed to sync threshold to Sense_Control.");
                            }
                        }
                    }

                    // 🔍 Try sensor
                    else if (SensorStorage.getSensors().containsKey(updateId)) {
                        Sensor sensor = SensorStorage.getSensors().get(updateId);

                        System.out.print("Enter new name (current: " + sensor.getSensorName() + "): ");
                        String newName = scanner.nextLine().trim();

                        System.out.print("Enter new unit (current: " + sensor.getUnit() + "): ");
                        String newUnit = scanner.nextLine().trim();

                        System.out.print("Enter new default value (current: " + sensor.getCurrentValue() + "): ");
                        String defaultStr = scanner.nextLine().trim();

                        if (!newName.isEmpty()) sensor.sensorName = newName;
                        if (!newUnit.isEmpty()) sensor.unit = newUnit;
                        if (!defaultStr.isEmpty()) {
                            try {
                                int val = Integer.parseInt(defaultStr);
                                sensor.simulateValue(val);
                            } catch (NumberFormatException e) {
                                System.out.println("⚠️ Invalid number. Keeping previous value.");
                            }
                        }

                        sensor.updateTimestamp();
                        boolean updated = XlCreator.updateSensor(sensor);
                        System.out.println(updated ? "✅ Sensor updated." : "❌ Sensor update failed.");
                    }

                    // ❌ ID not found
                    else {
                        System.out.println("❌ ID not found.");
                    }
                }



                case "4" -> {
                    DeviceViewer.displayAllDevicesAndSensors();
                    System.out.print("Enter ID of the device or sensor to remove: ");
                    String removeId = scanner.nextLine().trim();

                    boolean removed =
                            DeviceStorage.getDevices().containsKey(removeId)
                                    ? XlCreator.removeDevice(removeId)
                                    : XlCreator.removeSensor(removeId);

                    System.out.println(removed ? "🗑️ Removed successfully." : "❌ Could not remove. ID not found.");
                }

                case "5" -> back = true;
                default -> System.out.println("❌ Invalid option. Please choose 1-5.");
            }
        }
    }

}