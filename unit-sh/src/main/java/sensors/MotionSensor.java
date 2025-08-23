package sensors;

import java.time.Clock;

/**
 * 🕵️ MotionSensor - Detects movement in a defined area.
 * Supports simulation, automation triggers, and real-time updates.
 */
public class MotionSensor extends Sensor {

    private boolean motionDetected;

    public MotionSensor(String sensorId, String sensorName, MeasurementUnit unit, double currentValue, Clock clock) {
        super(sensorId, SensorType.MOTION, sensorName, unit, currentValue, clock);
        this.motionDetected = currentValue > 0;
        validateUnit(unit);
    }

    // 🛡️ Ensures unit is always BOOLEAN or NONE
    private void validateUnit(MeasurementUnit unit) {
        if (unit != MeasurementUnit.BOOLEAN && unit != MeasurementUnit.NONE) {
            System.out.println("⚠️ MotionSensor initialized with unsupported unit. Forcing to BOOLEAN.");
            this.unit = MeasurementUnit.BOOLEAN;
        }
    }

    // 📡 Simulates reading from a real motion detector
    @Override
    public double readCurrentValue() {
        return motionDetected ? 1.0 : 0.0;
    }

    // 📊 Returns 1.0 if motion is detected, 0.0 otherwise
    @Override
    public double getCurrentReading() {
        return readCurrentValue();
    }

    // 🧪 Simulates motion detection
    @Override
    public void simulateValue(double value) {
        this.motionDetected = value > 0;
        this.currentValue = readCurrentValue();
        updateTimestamp();
        notifyLinkedDevices(currentValue);
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    @Override
    public String toString() {
        return String.format("🕵️ MotionSensor [%s] %s → %s", sensorId, sensorName, motionDetected ? "Motion Detected" : "No Motion");
    }
}
