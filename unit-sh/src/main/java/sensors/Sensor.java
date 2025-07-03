package sensors;

import devices.Device;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Sensor implements Runnable {

    // ─── 🔑 Identity & Metadata ───
    protected final String sensorId;
    public String sensorName;
    public String unit;
    protected final SensorType type;

    // ─── ⚙️ Runtime State ───
    protected int currentValue;
    protected final List<Device> slaves = new ArrayList<>();
    protected final Clock clock;

    // ─── 🕒 Timestamps ───
    protected final ZonedDateTime createdTimestamp;
    protected ZonedDateTime updatedTimestamp;
    protected ZonedDateTime removedTimestamp;

    // ─── 🏗 Constructor ───
    public Sensor(String sensorId, SensorType type, String sensorName, String unit, int defaultValue, Clock clock) {
        this.sensorId = sensorId;
        this.type = type;
        this.sensorName = sensorName;
        this.unit = unit;
        this.clock = clock;
        this.createdTimestamp = ZonedDateTime.now(clock);
        this.updatedTimestamp = createdTimestamp;
        this.currentValue = defaultValue;
    }

    // ─── 📡 Core AutoOp Logic ───
    public void notifySlaves(double value) {
        System.out.println("🔔 notifySlaves → Reading: " + value + " | Slaves: " + slaves.size());

        for (Device device : slaves) {
            if (!device.isAutomationEnabled()) {
                System.out.println("⛔ Skipped (AutoOp disabled): " + device.getId());
                continue;
            }

            System.out.printf("🔍 %s [%s] | THRESHOLD: %.1f | Ref: %s%n",
                    device.getId(),
                    device.getClass().getSimpleName(),
                    device.getAutoOnThreshold(),
                    System.identityHashCode(device));

            if (value < device.getAutoOnThreshold()) {
                device.turnOn();
                System.out.println("💡 Auto ON triggered for " + device.getName());
            } else {
                device.turnOff();
                System.out.println("🌙 Auto OFF triggered for " + device.getName());
            }
        }
    }

    // ─── 🔁 Live I/O (abstract) ───
    public abstract int readCurrentValue();
    public abstract void simulateValue(int value);
    public abstract int getCurrentReading();

    // ─── 📎 Linking ───
    public void addSlave(Device device) {
        slaves.add(device);
    }

    public List<Device> getSlaves() {
        return slaves;
    }

    // ─── 🧪 Simulation & Test Tools ───
    public void testSensorBehavior() {
        System.out.println("🔍 Starting test for sensor: " + sensorId);
        int original = readCurrentValue();

        try {
            gradualChange(original, TEST_MIN_VALUE, TEST_STEP_COUNT);
            Thread.sleep(TEST_HOLD_DURATION_MS);
            gradualChange(TEST_MIN_VALUE, TEST_MAX_VALUE, TEST_STEP_COUNT);
            Thread.sleep(TEST_HOLD_DURATION_MS);
            gradualChange(TEST_MAX_VALUE, original, TEST_STEP_COUNT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("⚠️ Sensor test interrupted.");
        }

        System.out.println("✅ Sensor test completed.");
    }

    protected void gradualChange(int from, int to, int steps) throws InterruptedException {
        int step = (to - from) / steps;
        for (int i = 0; i <= steps; i++) {
            int value = from + i * step;
            simulateValue(value);
            notifySlaves(value);
            updateTimestamp();
            Thread.sleep(100);
        }
    }

    // ─── ⏳ Timestamp Logic ───
    public void updateTimestamp() {
        this.updatedTimestamp = ZonedDateTime.now(clock);
    }

    public void markAsRemoved() {
        this.removedTimestamp = ZonedDateTime.now(clock);
    }

    public String getCreatedTimestamp() {
        return (createdTimestamp != null) ? createdTimestamp.toString() : "N/A";
    }

    public String getUpdatedTimestamp() {
        return (updatedTimestamp != null) ? updatedTimestamp.toString() : "N/A";
    }

    public String getRemovedTimestamp() {
        return (removedTimestamp != null) ? removedTimestamp.toString() : "N/A";
    }

    // ─── 📖 Getters ───
    public String getSensorId() {
        return sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public SensorType getSensorType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    // ─── ☎️ Runnable Defaults ───
    @Override
    public void run() {
        testSensorBehavior();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %d %s",
                getClass().getSimpleName(), sensorId, sensorName, currentValue, unit);
    }

    // 🧪 Test Constants
    protected static final int DEFAULT_TEST_START_VALUE = 2000;
    protected static final int TEST_MIN_VALUE = 0;
    protected static final int TEST_MAX_VALUE = 4096;
    protected static final int TEST_STEP_COUNT = 20;
    protected static final int TEST_HOLD_DURATION_MS = 5000;
}
