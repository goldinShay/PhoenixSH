package sensors;

import devices.Device;
import devices.DeviceType;
import java.time.Clock;
import java.util.*;

public class LightSensor extends Sensor {

    // ✅ Slave device collection
    private final Set<Device> linkedDevices = new HashSet<>();

    public LightSensor(String sensorId, String name, MeasurementUnit unit, double currentValue, Clock clock) {
        super(sensorId, SensorType.LIGHT, name, unit, currentValue, clock);
    }

    @Override
    public double readCurrentValue() {
        return currentValue;
    }

    @Override
    public void simulateValue(double value) {
        this.currentValue = value;
        System.out.println("🔆 [LightSensor] " + sensorName + " simulated value: " + value + " " + unit);
        updateTimestamp();
        System.out.println("📦 Sensor '" + sensorName + "' has " + getLinkedDevice().size() + " slaves at simulation");
        notifyLinkedDevices(value);
    }

    public void notifyLinkedDevices(double value) {
        System.out.printf("📣 [LightSensor] Broadcasting value %.2f %s to linkedDevices...%n", value, unit);

        for (Device slave : linkedDevices) {
            if (slave == null || !slave.isAutomationEnabled()) continue;

            if (slave.getType() == DeviceType.LIGHT) {
                double threshold = slave.getAutoThreshold();
                boolean shouldTurnOn = value < threshold && !slave.isOn();
                boolean shouldTurnOff = value >= threshold && slave.isOn();

                if (shouldTurnOn) {
                    slave.turnOn();
                    System.out.println("💡 Auto ON triggered for: " + slave.getName());
                } else if (shouldTurnOff) {
                    slave.turnOff();
                    System.out.println("🌙 Auto OFF triggered for: " + slave.getName());
                }
            }
        }
    }

    @Override
    void internalAddLinkedDevice(Device device) {
        if (device != null && !linkedDevices.contains(device)) {
            linkedDevices.add(device);
            System.out.println("🔗 [LightSensor] Linked: " + device.getName());
        } else {
            System.out.println("⚠️ [LightSensor] Skipped linking null or duplicate device");
        }
    }

    @Override
    public List<Device> getLinkedDevice() {
        return Collections.unmodifiableList(new ArrayList<>(linkedDevices)); // Converts Set → List
    }


    @Override
    public double getCurrentReading() {
        return currentValue;
    }

    @Override
    public String toString() {
        return "[LightSensor] " + sensorName + " (" + sensorId + ") - Current: " + currentValue + " " + unit;
    }
}
