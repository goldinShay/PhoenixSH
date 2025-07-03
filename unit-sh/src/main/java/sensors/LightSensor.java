package sensors;

import devices.Device;
import devices.DeviceType;

import java.time.Clock;

public class LightSensor extends Sensor {

    public LightSensor(String sensorId, String name, String unit, int defaultValue, Clock clock) {
        super(sensorId, SensorType.LIGHT, name, unit, defaultValue, clock); // ✅ Now matches parent
    }


    @Override
    public int readCurrentValue() {
        return currentValue;  // In a real-world scenario, you'd access hardware or service APIs here
    }

    @Override
    public void simulateValue(int value) {
        this.currentValue = value;
        System.out.println("🔆 [LightSensor] " + sensorName + " simulated value: " + value + " " + unit);
        updateTimestamp();
        notifySlaves(value); // ⚡ Trigger automation logic for linked devices
    }
    public void notifySlaves(int value) {
        System.out.println("📣 [LightSensor] Broadcasting value " + value + " " + unit + " to slaves...");

        for (Device slave : slaves) {
            if (slave == null || !slave.isAutomationEnabled()) continue;

            // Only target lights
            if (slave.getType() == DeviceType.LIGHT) {
                String name = slave.getName();
                double threshold = slave.getAutoOnThreshold();

                if (value < threshold && !slave.isOn()) {
                    slave.turnOn();
                    System.out.println("💡 Auto ON triggered for: " + name);
                } else if (value >= threshold && slave.isOn()) {
                    slave.turnOff();
                    System.out.println("🌙 Auto OFF triggered for: " + name);
                }
            }
        }
    }

    @Override
    public int getCurrentReading() {
        return currentValue; // This keeps the logic consistent with simulateValue()
    }

    @Override
    public String toString() {
        return "[LightSensor] " + sensorName + " (" + sensorId + ") - Current: " + currentValue + " " + unit;
    }
}
