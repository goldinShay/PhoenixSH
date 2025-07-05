package storage;

import devices.Device;
import sensors.Sensor;
import storage.xlc.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XlCreator {

    private static final XlDeviceManager deviceManager = new XlDeviceManager();
    private static final XlSensorManager sensorManager = new XlSensorManager();
    private static final XlSenseControlManager senseControlManager = new XlSenseControlManager();
    private static final XlTaskSchedulerManager schedulerManager = new XlTaskSchedulerManager();

    // ----- Device Delegates -----

    public static List<Device> loadDevicesFromExcel() {
        return deviceManager.loadDevicesFromExcel();
    }

    public static void writeDeviceToExcel(Device device) throws IOException {
        deviceManager.writeDeviceToExcel(device);
    }

    public static boolean updateDevice(Device device) {
        return deviceManager.updateDevice(device);
    }

    public static boolean removeDevice(String deviceId) {
        return deviceManager.removeDevice(deviceId);
    }

    public static String getNextAvailableId(String prefix, Set<String> existingIds) {
        return deviceManager.getNextAvailableId(prefix, existingIds);
    }

    // ----- Sensor Delegates -----

    public static Map<String, Sensor> loadSensors() {
        return sensorManager.loadSensors();
    }

    public static boolean writeSensorToExcel(Sensor sensor) {
        return sensorManager.writeSensorToExcel(sensor);
    }

    public static boolean updateSensor(Sensor sensor) {
        return sensorManager.updateSensor(sensor);
    }

    public static boolean removeSensor(String sensorId) {
        return sensorManager.removeSensor(sensorId);
    }

    // ----- Sense Control Delegates -----

    public static boolean appendToSenseControl(Device slave, Sensor master) {
        return senseControlManager.appendToSenseControlSheet(slave, master);
    }

    public static boolean removeFromSenseControl(String slaveId) {
        return senseControlManager.removeFromSenseControlSheet(slaveId);
    }

    public static boolean updateAutoOpThresholds(String deviceId, double newOn, double ignoredOff) {
        return senseControlManager.updateAutoOpThresholds(deviceId, newOn, ignoredOff);
    }

    public static void loadSensorLinks(Map<String, Device> devices, Map<String, Sensor> sensors) {
        senseControlManager.loadSensorLinksFromExcel(devices, sensors);
    }

    // ----- Scheduler Delegates -----

    public static List<Map<String, String>> viewTasks() {
        return schedulerManager.loadTasks();
    }

    public static boolean addTask(String deviceId, String name, String action, String when, String repeat) {
        return schedulerManager.addTask(deviceId, name, action, when, repeat);
    }

    public static boolean updateTask(String deviceId, String newWhen, String newRepeat) {
        return schedulerManager.updateTask(deviceId, newWhen, newRepeat);
    }

    public static boolean deleteTask(String deviceId) {
        return schedulerManager.deleteTask(deviceId);
    }
    public static boolean removeSensorLink(String slaveId) {
        return XlSenseControlManager.removeSensorLink(slaveId);
    }
    public static boolean createNewWorkbook() {
        // You'll likely want to call the utility or initializer method here
        return XlWorkbookUtils.ensureFileExists(); // or whatever method does the actual creation
    }


}
