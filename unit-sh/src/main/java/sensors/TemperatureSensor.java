package sensors;

import java.time.Clock;

/**
 * 🌡️ TemperatureSensor - Measures ambient temperature in Celsius.
 * Supports simulation, automation linkage, and real-time updates.
 */
public class TemperatureSensor extends Sensor {

    public TemperatureSensor(String sensorId, String sensorName, MeasurementUnit unit, double currentValue, Clock clock) {
        super(sensorId, SensorType.TEMPERATURE, sensorName, unit, currentValue, clock);
        validateUnit(unit);
    }

    // 🔍 Ensures unit is always Celsius
    private void validateUnit(MeasurementUnit unit) {
        if (unit != MeasurementUnit.CELSIUS) {
            System.out.println("⚠️ TemperatureSensor initialized with non-Celsius unit. Forcing to °C.");
            this.unit = MeasurementUnit.CELSIUS;
        }
    }

    // 📡 Simulates reading from a real sensor (could be replaced with hardware integration)
    @Override
    public double readCurrentValue() {
        return currentValue; // In real use, replace with actual sensor input
    }

    // 📊 Returns the current temperature reading
    @Override
    public double getCurrentReading() {
        return currentValue;
    }

    // 🧪 Simulates a new temperature value
    @Override
    public void simulateValue(double value) {
        if (value < -50 || value > 100) {
            System.out.println("⚠️ Simulated temperature out of realistic bounds: " + value + "°C");
        }
        this.currentValue = value;
        updateTimestamp();
        notifyLinkedDevices(value);
    }

    @Override
    public String toString() {
        return String.format("🌡️ TemperatureSensor [%s] %s → %.1f°C", sensorId, sensorName, currentValue);
    }
}