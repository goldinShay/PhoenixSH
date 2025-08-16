package sensors;

import devices.Device;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

public abstract class Sensor implements Runnable {

    // ─── 🔑 Identity ───
    protected final String sensorId;
    protected String sensorName;
    protected MeasurementUnit unit;
    protected final SensorType type;

    // ─── ⚙️ Runtime ───
    protected double currentValue;
    protected final Clock clock;

    // ─── 🔗 Device Linkage ───
    private final List<Device> linkedDevices = new ArrayList<>();

    // ─── 🕒 Timestamps ───
    protected final ZonedDateTime createdTimestamp;
    protected ZonedDateTime updatedTimestamp;
    protected ZonedDateTime removedTimestamp;

    // ─── 🏗 Constructor ───
    public Sensor(String sensorId, SensorType type, String sensorName, MeasurementUnit unit, double currentValue, Clock clock) {
        this.sensorId = sensorId;
        this.type = type;
        this.sensorName = sensorName;
        this.unit = unit;
        this.currentValue = currentValue;
        this.clock = clock;
        this.createdTimestamp = ZonedDateTime.now(clock);
        this.updatedTimestamp = createdTimestamp;
    }

    // ─── 📎 Linking Control ───
    public boolean isAlreadyLinkedTo(Device device) {
        return linkedDevices.contains(device);
    }

    void internalAddLinkedDevice(Device device) {
        if (device != null && !linkedDevices.contains(device)) {
            linkedDevices.add(device);
            System.out.println("🔗 [Sensor] Linked: " + device.getName());
        }
    }

    public void removeLinkedDevice(Device device) {
        if (linkedDevices.remove(device)) {
            System.out.printf("🧹 Removed '%s' from sensor '%s'%n", device.getId(), sensorId);
        }
    }
//    public void clearSlaves() {
//        slaveDevices.clear();
//    }

    public List<Device> getLinkedDevice() {
        return Collections.unmodifiableList(linkedDevices);
    }

    public int getLinkedDevicesCount() {
        return linkedDevices.size();
    }

    // ─── 📡 Automation ───
    public void notifyLinkedDevices(double value) {
        System.out.println("🔔 Notifying " + linkedDevices.size() + " linked devices");

        for (Device device : linkedDevices) {
            if (!device.isAutomationEnabled()) continue;

            if (value < device.getAutoThreshold()) {
                device.turnOn();
            } else {
                device.turnOff();
            }
        }
    }

    // ─── 🎛 Sensor Mechanics ───
    public abstract double readCurrentValue();
    public abstract double getCurrentReading();
    public abstract void simulateValue(double value);

    public void setCurrentValue(double value) {
        this.currentValue = value;
        updateTimestamp();
    }

    public void setSensorName(String name) {
        this.sensorName = name;
    }

    public void setUnit(MeasurementUnit unit) {
        if (unit != null && unit != MeasurementUnit.UNKNOWN) {
            this.unit = unit;
        }
    }

    // ─── 📊 Simulation Tools ───
    public void testSensorBehavior() {
        double original = readCurrentValue();
        try {
            gradualChange(original, TEST_MIN, TEST_STEPS);
            Thread.sleep(TEST_HOLD);
            gradualChange(TEST_MIN, TEST_MAX, TEST_STEPS);
            Thread.sleep(TEST_HOLD);
            gradualChange(TEST_MAX, original, TEST_STEPS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void gradualChange(double from, double to, int steps) throws InterruptedException {
        double step = (to - from) / steps;
        for (int i = 0; i <= steps; i++) {
            double value = from + i * step;
            simulateValue(value);
            notifyLinkedDevices(value);
            updateTimestamp();
            Thread.sleep(100);
        }
    }

    // ─── ⏱ Timestamping ───
    public void updateTimestamp() {
        updatedTimestamp = ZonedDateTime.now(clock);
    }

    public String getCreatedTimestamp() {
        return createdTimestamp.toString();
    }

    public String getUpdatedTimestamp() {
        return (updatedTimestamp != null) ? updatedTimestamp.toString() : "N/A";
    }

    public String getRemovedTimestamp() {
        return (removedTimestamp != null) ? removedTimestamp.toString() : "N/A";
    }

    // ─── 📖 Accessors ───
    public String getSensorId() {
        return sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public SensorType getSensorType() {
        return type;
    }

    public MeasurementUnit getUnit() {
        return unit;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    // ─── ☎️ Runnable & Debug ───
    @Override
    public void run() {
        testSensorBehavior();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %.1f %s", getClass().getSimpleName(), sensorId, sensorName, currentValue, unit);
    }
    public final void linkLinkedDevice(Device device) {
        System.out.println(" linking to device: " + device.getId());

        if (device != null && !linkedDevices.contains(device)) {
            internalAddLinkedDevice(device);
        }
    }

    // ─── 🧪 Constants ───
    protected static final int TEST_MIN = 0;
    protected static final int TEST_MAX = 4096;
    protected static final int TEST_STEPS = 20;
    protected static final int TEST_HOLD = 3000;
}
