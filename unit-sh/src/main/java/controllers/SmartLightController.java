package controllers;

import autoOp.AutoOpUnlinker;
import devices.SmartLight;
import devices.actions.DeviceAction;
import org.apache.poi.ss.usermodel.Workbook;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import storage.xlc.XlDeviceManager;
import storage.xlc.XlSmartLightManager;
import storage.xlc.XlWorkbookUtils;
import utils.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmartLightController {

    /**
     * Updates both the Devices and Smart_Light_Control sheets with the latest SmartLight data.
     * Ensures AutoOp settings are synchronized across both sheets.
     */
    public static boolean updateSmartLight(SmartLight light) {
        if (light == null) {
            Log.error("❌ Cannot update null SmartLight.");
            return false;
        }

        File file = XlWorkbookUtils.getFilePath().toFile();
        if (!file.exists() || !XlWorkbookUtils.isExcelFileHealthy(file)) {
            Log.error("❌ Workbook file is missing or corrupt.");
            return false;
        }

        Workbook workbook = XlWorkbookUtils.loadWorkbook();
        if (workbook == null) {
            Log.error("❌ Failed to load workbook.");
            return false;
        }

        // 🛡️ Restore supported actions only if missing — without reconstructing the device
//        if (light.getSupportedActionsAsText() == null || light.getSupportedActionsAsText().isBlank()) {
//            List<DeviceAction> actions = DeviceAction.getActionsForDevice(light.getId());
//            if (actions != null && !actions.isEmpty()) {
//                String actionsText = actions.stream()
//                        .map(DeviceAction::name)
//                        .collect(Collectors.joining(","));
//                light.setSupportedActionsFromText(actionsText);
//                Log.info("🔄 Restored supported actions from memory for SmartLight '" + light.getId() + "'");
//            } else {
//                Log.warn("⚠️ No supported actions found in memory for SmartLight '" + light.getId() + "'");
//            }
//        }

        boolean controlUpdated = XlSmartLightManager.updateSmartLight(workbook, light);
        boolean deviceUpdated;
        try {
            deviceUpdated = XlDeviceManager.updateDevice(light);
        } catch (IOException e) {
            e.printStackTrace();
            Log.error("❌ Failed to update device row for SmartLight: " + light.getId());
            return false;
        }

        if (controlUpdated && deviceUpdated) {
            Log.info("✅ SmartLight updated in both sheets: " + light.getId());
            return true;
        } else {
            Log.warn("⚠️ Partial update for SmartLight: " + light.getId());
            return false;
        }
    }



    /**
     * Links a SmartLight to a Sensor and persists the AutoOp configuration.
     * Updates both sheets and calls AutoOpManager to save the link.
     */
    public static boolean enableAutoOp(SmartLight light, Sensor sensor) {
        if (light == null || sensor == null) return false;

        Sensor canonicalSensor = SensorStorage.getSensor(sensor.getSensorId());
        if (canonicalSensor == null) {
            Log.error("❌ Sensor not found in SensorStorage: " + sensor.getSensorId());
            return false;
        }

        light.setAutomationEnabled(true);
        light.setAutomationSensorId(canonicalSensor.getSensorId());
        canonicalSensor.linkLinkedDevice(light);

        boolean persisted = XlCreator.appendToSenseControl(light, canonicalSensor);
        boolean updated = updateSmartLight(light);

        if (persisted && updated) {
            Log.info("🔗 AutoOp enabled and persisted for SmartLight: " + light.getId());
            return true;
        } else {
            Log.warn("⚠️ AutoOp link may be incomplete for SmartLight: " + light.getId());
            return false;
        }
    }
    /**
     * Removes AutoOp linkage and updates both sheets accordingly.
     */
    public static void disableAutoOp(SmartLight light) {
        if (light == null) return;

        AutoOpUnlinker.disable(light);
        Log.info("🧹 AutoOp disabled via AutoOpUnlinker for SmartLight: " + light.getId());
    }

    /**
     * Debug method to print SmartLight data from both sheets.
     */
    public static void debugSmartLight(String deviceId) {
        SmartLight light = DeviceStorage.getSmartLight(deviceId);
        if (light == null) {
            System.out.println("❌ No SmartLight found with ID: " + deviceId);
            return;
        }

        System.out.println("=== SmartLight Debug ===");
        System.out.println("ID: " + light.getId());
        System.out.println("Name: " + light.getName());
        System.out.println("Brand/Model: " + light.getBrand() + " / " + light.getModel());
        System.out.println("AutoOp: " + (light.isAutomationEnabled() ? "ENABLED" : "DISABLED"));
        System.out.println("Threshold: " + light.getAutoThreshold());
        System.out.println("Sensor Link: " + light.getAutomationSensorId());
        System.out.println("Color Mode: " + light.getColorMode().getLabel());
        System.out.println("RGB: " + light.getColorMode().getRed() + "," +
                light.getColorMode().getGreen() + "," +
                light.getColorMode().getBlue());
        System.out.println("FX Mode: " + light.getLiteFx().name());
        System.out.println("========================");
    }

    public static boolean linkSensor(SmartLight light, Sensor sensor) {
        if (light == null || sensor == null) {
            System.err.println("❌ Cannot link: SmartLight or Sensor is null.");
            return false;
        }

        light.setAutomationEnabled(true);
        light.setAutomationSensorId(sensor.getSensorId());
        sensor.linkLinkedDevice(light);

        Workbook workbook = XlWorkbookUtils.loadWorkbook();
        boolean controlWritten = XlSmartLightManager.updateSmartLight(workbook, light);
        boolean senseWritten = XlCreator.appendToSenseControl(light, sensor);

        if (controlWritten && senseWritten) {
            System.out.printf("🔗 '%s' successfully linked to sensor '%s'.%n",
                    light.getName(), sensor.getSensorName());
            return true;
        }

        System.err.println("⚠️ Failed to write SmartLight link to Excel.");
        return false;
    }

    /**
     * Restores a SmartLight's AutoOp link from persisted data.
     */
    public static void restoreLink(SmartLight light, Sensor sensor, double autoOn, double autoOff) {
        if (light == null || sensor == null) {
            Log.warn("❌ Cannot restore link: " +
                    (light == null ? "SmartLight=null " : "") +
                    (sensor == null ? "Sensor=null" : ""));
            return;
        }

        try {
            light.setAutoThreshold(autoOn, true);
            light.setAutoThreshold(autoOff, true);
            light.setAutomationEnabled(true);
            light.setAutomationSensorId(sensor.getSensorId());

            if (!sensor.getLinkedDevice().contains(light)) {
                sensor.linkLinkedDevice(light);
            }

            Log.debug("🔗 SmartLight restored → " + light.getId() +
                    " linked to sensor " + sensor.getSensorId() +
                    " | ON=" + autoOn + " | OFF=" + autoOff);

        } catch (Exception e) {
            Log.error("❌ Failed to restore SmartLight link: " + e.getMessage());
        }
    }
    private static final Map<String, SmartLight> smartLights = new HashMap<>();

    public static void register(SmartLight sl) {
        smartLights.put(sl.getId(), sl);
    }

    public static SmartLight get(String id) {
        return smartLights.get(id);
    }

    public static Collection<SmartLight> getAll() {
        return smartLights.values();
    }
}