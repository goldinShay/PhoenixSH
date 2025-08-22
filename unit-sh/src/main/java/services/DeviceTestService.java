package services;

import devices.Device;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;

public class DeviceTestService {

    public static boolean testDeviceById(String deviceId) {
        Device device = DeviceStorage.getDevices().get(deviceId);
        if (device == null) {
            System.out.println("❌ No device found with ID: " + deviceId);
            return false;
        }

        if (device.isOn()) {
            System.out.println("❌ Device is already ON. Turn it off before testing.");
            return false;
        }

        device.testDevice();
        return true;
    }

    public static boolean testSensorById(String sensorId) {
        Sensor sensor = SensorStorage.getSensors().get(sensorId);
        if (sensor == null) {
            System.out.println("❌ No sensor found with ID: " + sensorId);
            return false;
        }

        sensor.testSensorBehavior();
        return true;
    }
}