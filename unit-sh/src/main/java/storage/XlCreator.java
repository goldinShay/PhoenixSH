package storage;

import devices.Device;
import sensors.Sensor;
import storage.xlc.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class XlCreator {

    private static final XlDeviceManager deviceManager = new XlDeviceManager();
    private static final XlSensorManager sensorManager = new XlSensorManager();
    private static final XlSenseControlManager senseControlManager = new XlSenseControlManager();
    private static final XlTaskSchedulerManager schedulerManager = new XlTaskSchedulerManager();

    // 🧪 Optional test stubs
    private static Function<Device, Boolean> deviceUpdater = null;
    private static Function<String, Boolean> deviceRemover = null;
    private static Function<Sensor, Boolean> sensorUpdater = null;
    private static Function<DeviceSensorPair, Boolean> senseAppender = null;
    private static Function<String, Boolean> senseRemover = null;

    // ----- Hook Injectors -----
    public static void setDeviceUpdater(Function<Device, Boolean> f) { deviceUpdater = f; }
    public static void setDeviceRemover(Function<String, Boolean> f) { deviceRemover = f; }
    public static void setSensorUpdater(Function<Sensor, Boolean> f) { sensorUpdater = f; }

    // ----- Device Delegates -----
    public static List<Device> loadDevicesFromExcel() {
        return deviceManager.loadDevicesFromExcel();
    }

    public static void writeDeviceToExcel(Device device) throws IOException {
        deviceManager.writeDeviceToExcel(device);
    }

    public static boolean updateDevice(Device device) {
        return (deviceUpdater != null) ? deviceUpdater.apply(device)
                : deviceManager.updateDevice(device);
    }

    public static boolean removeDevice(String deviceId) {
        return (deviceRemover != null) ? deviceRemover.apply(deviceId)
                : deviceManager.removeDevice(deviceId);
    }

    // ----- Sensor Delegates -----
    public static Map<String, Sensor> loadSensors() {
        return sensorManager.loadSensors();
    }

    public static boolean writeSensorToExcel(Sensor sensor) {
        return sensorManager.writeSensorToExcel(sensor);
    }

    public static boolean updateSensor(Sensor sensor) {
        return (sensorUpdater != null) ? sensorUpdater.apply(sensor)
                : sensorManager.updateSensor(sensor);
    }

    public static boolean removeSensor(String sensorId) {
        return sensorManager.removeSensor(sensorId);
    }

    // ----- Sense Control Delegates -----
    public static boolean appendToSenseControl(Device slave, Sensor master) {
        return (senseAppender != null) ? senseAppender.apply(new DeviceSensorPair(slave, master))
                : senseControlManager.appendToSenseControlSheet(slave, master);
    }

    public static boolean removeFromSenseControl(String slaveId) {
        return (senseRemover != null) ? senseRemover.apply(slaveId)
                : senseControlManager.removeFromSenseControlSheet(slaveId);
    }

    public static boolean updateAutoOpThresholds(String deviceId, double newOn, double ignoredOff) {
        return senseControlManager.updateAutoOpThresholds(deviceId, newOn, ignoredOff);
    }

    public static void loadSensorLinks(Map<String, Device> devices, Map<String, Sensor> sensors) {
        senseControlManager.loadSensorLinksFromExcel(devices, sensors);
    }

    public static boolean removeSensorLink(String slaveId) {
        return XlSenseControlManager.removeSensorLink(slaveId);
    }

    public static boolean createNewWorkbook() {
        return XlWorkbookUtils.ensureFileExists();
    }

    // 🔗 Simple pair holder for test stubbing
    public static class DeviceSensorPair {
        public final Device slave;
        public final Sensor master;

        public DeviceSensorPair(Device slave, Sensor master) {
            this.slave = slave;
            this.master = master;
        }
    }
}
