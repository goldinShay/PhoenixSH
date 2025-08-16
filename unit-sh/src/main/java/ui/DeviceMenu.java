package ui;

import devices.Device;
import sensors.Sensor;
import sensors.SensorFactory;
import sensors.MeasurementUnit;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import storage.xlc.XlAutoOpManager;
import storage.xlc.XlSensorManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DeviceMenu {
    public static void DevicesMenu(Map<String, Device> devices, List<Thread> deviceThreads) {
        XlCreator xlCreator = new XlCreator(); // ✅ Instance-based usage
        Scanner scanner = new Scanner(System.in); // 🔄 NEW: fresh scanner tied to the current System.in
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

                    if (DeviceStorage.getDevices().containsKey(updateId)) {
                        Device device = DeviceStorage.getDevices().get(updateId);

                        System.out.print("Enter new name (current: " + device.getName() + "): ");
                        String newName = scanner.nextLine().trim();

                        System.out.print("Enter new brand (current: " + device.getBrand() + "): ");
                        String newBrand = scanner.nextLine().trim();

                        System.out.print("Enter new model (current: " + device.getModel() + "): ");
                        String newModel = scanner.nextLine().trim();

                        System.out.print("Enter new Auto-ON threshold (current: " + device.getAutoThreshold() + ") or leave blank to keep: ");
                        String newOnStr = scanner.nextLine().trim();

                        device.setName(!newName.isEmpty() ? newName : device.getName());
                        device.setBrand(!newBrand.isEmpty() ? newBrand : device.getBrand());
                        device.setModel(!newModel.isEmpty() ? newModel : device.getModel());

                        boolean thresholdChanged = false;
                        if (!newOnStr.isEmpty()) {
                            if (newOnStr.equalsIgnoreCase("reset") || newOnStr.equalsIgnoreCase("r")) {
                                device.resetAutoThreshold();
                                device.resetAutoThreshold(); // double for good measure?
                            } else {
                                try {
                                    double newVal = Double.parseDouble(newOnStr);
                                    device.setAutoThreshold(newVal, true);
                                    device.setAutoThreshold(newVal, true);
                                } catch (NumberFormatException e) {
                                    System.out.println("⚠️ Invalid threshold. Keeping previous value.");
                                }
                            }
                            thresholdChanged = true;
                        }

                        device.updateTimestamp();

                        boolean updated = xlCreator.delegateDeviceUpdate(device); // ✅ now clean
                        System.out.println(updated ? "✅ Device updated." : "❌ Update failed.");

                        // 🔁 Sync threshold to Sens_Ctrl (mirroring ON → OFF)
                        if (thresholdChanged && device.getAutomationSensorId() != null) {
                            boolean synced = xlCreator.updateAutoOpThresholds(
                                    device.getId(),
                                    device.getAutoThreshold(),
                                    device.getAutoThreshold()
                            );
                            System.out.println(synced
                                    ? "🔄 Sense_Control threshold synced (AUTO-OFF mirrored)."
                                    : "⚠️ Failed to sync threshold to Sense_Control.");
                        }

                    } else if (SensorStorage.getSensors().containsKey(updateId)) {
                        Sensor sensor = SensorStorage.getSensors().get(updateId);

                        System.out.print("Enter new name (current: " + sensor.getSensorName() + "): ");
                        String newName = scanner.nextLine().trim();

                        System.out.print("Enter new unit (current: " + sensor.getUnit() + "): ");
                        String newUnit = scanner.nextLine().trim();

                        System.out.print("Enter new default value (current: " + sensor.getCurrentValue() + "): ");
                        String defaultStr = scanner.nextLine().trim();

                        if (!newName.isEmpty()) sensor.setSensorName(newName);

                        if (!newUnit.isEmpty()) {
                            try {
                                MeasurementUnit parsedUnit = MeasurementUnit.valueOf(newUnit.toUpperCase());
                                if (parsedUnit != MeasurementUnit.UNKNOWN) {
                                    sensor.setUnit(parsedUnit);
                                }
                            } catch (IllegalArgumentException e) {
                                System.out.println("⚠️ Invalid unit. Keeping previous value.");
                            }
                        }

                        // 🔗 Fallback slave linkage BEFORE simulation
                        // 🔗 Fallback slave linkage BEFORE simulation
//                        Device linkedSlave = DeviceStorage.getLinkedDevice(sensor.getSensorId());
//                        if (linkedSlave == null) {
//                            Device fallbackSlave = DeviceStorage.getDevices().get("SL001"); // Update with your actual slave ID
//                            if (fallbackSlave != null) {
//                                sensor.linkSlave(fallbackSlave); // ✅ Clean, authorized, and safe
//                                DeviceStorage.linkSensorToDevice(fallbackSlave.getId(), sensor); // 🔁 Enables reverse lookup
//                                System.out.println("🔄 Fallback slave " + fallbackSlave.getId() + " linked to sensor " + sensor.getSensorId());
//                            } else {
//                                System.out.println("⚠️ No fallback device found. Sensor broadcast may be incomplete.");
//                            }
//                        }

                        if (!defaultStr.isEmpty()) {
                            try {
                                int val = Integer.parseInt(defaultStr);
                                sensor.simulateValue(val);
                            } catch (NumberFormatException e) {
                                System.out.println("⚠️ Invalid number. Keeping previous value.");
                            }
                        }

                        sensor.updateTimestamp();

                        boolean updatedMemory = XlCreator.updateSensor(sensor);
                        Device slave = DeviceStorage.getLinkedDevice(sensor.getSensorId());

                        System.out.println("🔍 Lookup: Linked slave for " + sensor.getSensorId() + " → " +
                                (slave == null ? "null ❌" : slave.getId() + " ✅"));

                        boolean updatedSheet = XlSensorManager.updateSensorSheet(sensor);
                        System.out.println("📋 Preparing to write control sheet entry:");
                        System.out.println("→ Device: " + (slave != null ? slave.getId() : "null"));
                        System.out.println("→ Sensor: " + (sensor != null ? sensor.getSensorId() : "null"));
                        boolean updatedCtrl = XlAutoOpManager.appendToSenseControlSheet(slave, sensor);

                        boolean allUpdated = updatedMemory && updatedSheet && updatedCtrl;

                        if (!allUpdated) {
                            System.out.println("❗One or more updates failed: " +
                                    "Memory=" + updatedMemory +
                                    ", Sheet=" + updatedSheet +
                                    ", Ctrl=" + updatedCtrl);
                        }

                        System.out.println(allUpdated
                                ? "✅ Sensor updated and all sheets synced."
                                : "❌ Sensor update incomplete.");
                    }
                    else {
                        System.out.println("❌ ID not found.");
                    }
                }

                case "4" -> {
                    DeviceViewer.displayAllDevicesAndSensors();
                    System.out.print("Enter ID of the device or sensor to remove: ");
                    String removeId = scanner.nextLine().trim();

                    boolean isDevice = DeviceStorage.getDevices().containsKey(removeId);
                    boolean isSensor = SensorStorage.getSensors().containsKey(removeId);

                    if (!isDevice && !isSensor) {
                        System.out.println("❌ No device or sensor found with ID: " + removeId);
                        break;
                    }

                    System.out.printf("⚠️ Are you sure you want to remove '%s'? This action is irreversible. (yes/no): ", removeId);
                    String confirmation = scanner.nextLine().trim().toLowerCase();

                    if (!confirmation.equals("yes")) {
                        System.out.println("🛑 Removal cancelled.");
                        break;
                    }

                    boolean removed = false;

                    if (isDevice) {
                        removed = XlCreator.removeDevice(removeId);
                        DeviceStorage.getDevices().remove(removeId);
                    } else if (isSensor) {
                        removed = XlCreator.removeSensor(removeId);
                        SensorStorage.getSensors().remove(removeId);
                        SensorFactory.clearSensorById(removeId); // If you added this method
                    }

                    System.out.println(removed
                            ? "🗑️ Removed successfully."
                            : "❌ Could not remove. Something went wrong.");
                }


                case "5" -> back = true;
                default -> System.out.println("❌ Invalid option. Please choose 1-5.");
            }
        }
    }
}
