package utils;

import devices.Device;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;

import java.io.FileInputStream;
import java.io.IOException;

public class AutoOpManager {

    // 📥 Load AutoOp links from Excel's Sense_Control sheet (on system startup)
    public static void loadMappingsFromExcel() {
        try (FileInputStream fis = new FileInputStream(XlCreator.getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet controlSheet = workbook.getSheet("Sense_Control");
            if (controlSheet == null) {
                System.out.println("⚠️ Sense_Control sheet not found.");
                return;
            }

            for (Row row : controlSheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String slaveId = row.getCell(1).getStringCellValue().trim();
                    String sensorId = row.getCell(3).getStringCellValue().trim();

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

                    // 🔁 Restore thresholds from Excel (Sense_Control)
                    double autoOn = row.getCell(4).getNumericCellValue();
                    double autoOff = row.getCell(5).getNumericCellValue();
                    slave.setAutoOnThreshold(autoOn, true);   // Marks as user-defined
                    slave.setAutoOffThreshold(autoOff);

                    System.out.printf("AutoOp threshold for %s: ON=%.1f | OFF=%.1f%n",
                            slave.getId(), slave.getAutoOnThreshold(), slave.getAutoOffThreshold());

                    // 🧠 Reactivate automation state in memory
                    slave.setAutomationEnabled(true);
                    slave.setAutomationSensorId(sensorId);

                    // 🔒 Only add if not already present
                    if (!master.getSlaves().contains(slave)) {
                        master.addSlave(slave);
                    }

                    System.out.printf("🔗 Restored link → %s → %s | AutoOp: %b | ON: %.1f | OFF: %.1f | Ref: %s%n",
                            slave.getId(), master.getSensorId(), slave.isAutomationEnabled(),
                            slave.getAutoOnThreshold(), slave.getAutoOffThreshold(),
                            System.identityHashCode(slave));

                } catch (Exception ex) {
                    System.out.printf("⚠️ Error restoring row %d → %s%n", row.getRowNum(), ex.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("🛑 Failed to read Sense_Control sheet: " + e.getMessage());
        }
    }


    // 🔗 Save a new AutoOp link to Excel
    public static boolean persistLink(Device slave, Sensor master) {
        boolean written = XlCreator.appendToSenseControlSheet(slave, master);
        if (!written) {
            System.out.println("⚠️ Failed to persist AutoOp link between " + slave.getName() + " and " + master.getSensorName());
            return false;
        }
        System.out.println("💾 AutoOp link saved to Sense_Control sheet.");
        return true;
    }

    // ❌ Remove a broken AutoOp link from Excel
    public static boolean unlink(Device slave) {
        boolean removed = XlCreator.removeFromSenseControlSheet(slave.getId());
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
        loadMappingsFromExcel(); // your actual Excel parse logic will go here
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

            // ✅ Now safely trigger device logic
            sensor.notifySlaves(value);
        }
    }


}
