package utils;

import devices.Device;
import sensors.Sensor;
import storage.AutoOpExcelReader;
import storage.AutoOpExcelReader.AutoOpRecord;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;

import java.util.List;

public class AutoOpManager {

    // 📥 Load AutoOp links from Excel using external reader
    public static void loadMappingsFromExcel() {
        List<AutoOpRecord> links = AutoOpExcelReader.readLinks();
        if (links.isEmpty()) {
            System.out.println("⚠️ No mappings found in Excel.");
            return;
        }

        for (AutoOpRecord record : links) {
            String slaveId = record.slaveId();
            String sensorId = record.sensorId();
            double autoOn = record.autoOn();
            double autoOff = record.autoOff();

            Device slave = DeviceStorage.getDevices().get(slaveId);
            Sensor master = SensorStorage.getSensors().get(sensorId);

            if (slave == null) {
                System.out.printf("⚠️ Missing device in memory: %s%n", slaveId);
                continue;
            }

            if (master == null) {
                System.out.printf("⚠️ Missing sensor in memory: %s%n", sensorId);
                continue;
            }

            // 🔁 Restore thresholds from Excel
            slave.setAutoOnThreshold(autoOn, true);   // Marks as user-defined
            slave.setAutoOffThreshold(autoOff);

            System.out.printf("AutoOp threshold for %s: ON=%.1f | OFF=%.1f%n",
                    slave.getId(), slave.getAutoOnThreshold(), slave.getAutoOffThreshold());

            // 🧠 Reactivate automation state in memory
            slave.setAutomationEnabled(true);
            slave.setAutomationSensorId(sensorId);

            // 🔒 Add to sensor if not already present
            if (!master.getSlaves().contains(slave)) {
                master.addSlave(slave);
            }

            System.out.printf("🔗 Restored link → %s → %s | AutoOp: %b | ON: %.1f | OFF: %.1f | Ref: %s%n",
                    slave.getId(), master.getSensorId(), slave.isAutomationEnabled(),
                    slave.getAutoOnThreshold(), slave.getAutoOffThreshold(),
                    System.identityHashCode(slave));
        }
    }

    // 🔗 Save a new AutoOp link to Excel
    public static boolean persistLink(Device slave, Sensor master) {
        boolean written = XlCreator.appendToSenseControl(slave, master);
        if (!written) {
            System.out.println("⚠️ Failed to persist AutoOp link between " + slave.getName() + " and " + master.getSensorName());
            return false;
        }
        System.out.println("💾 AutoOp link saved to Sense_Control sheet.");
        return true;
    }

    // ❌ Remove a broken AutoOp link from Excel
    public static boolean unlink(Device slave) {
        boolean removed = XlCreator.removeFromSenseControl(slave.getId());
        if (!removed) {
            System.out.println("⚠️ Could not remove AutoOp entry for " + slave.getId());
            return false;
        }
        System.out.println("🧹 AutoOp link removed from Excel.");
        return true;
    }

    // 🧠 Refresh runtime links after Excel rehydrate
    public static void restoreMemoryLinks() {
        System.out.println("🔁 Restoring AutoOp memory mappings...");
        loadMappingsFromExcel();
    }

    // 🪄 Manual trigger if you want to rescan sensor thresholds
    public static void reevaluateAllSensors() {
        System.out.println("🔁 Reevaluating all sensors...");

        for (Sensor sensor : SensorStorage.getSensors().values()) {
            double value = sensor.getCurrentReading();
            System.out.printf("📡 Sensor %s | Reading: %.1f | Slaves: %d%n",
                    sensor.getSensorId(), value, sensor.getSlaves().size());

            for (Device slave : sensor.getSlaves()) {
                System.out.printf("   🔍 %s → AutoOp: %b | ON: %.1f | OFF: %.1f | Ref: %s%n",
                        slave.getId(), slave.isAutomationEnabled(),
                        slave.getAutoOnThreshold(), slave.getAutoOffThreshold(),
                        System.identityHashCode(slave));
            }

            sensor.notifySlaves(value);
        }
    }
}
