package devices;

import sensors.Sensor;
import storage.DeviceStorage;
import utils.DeviceDefaults;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

public abstract class Device implements Runnable {

    // 🔐 ID enforcement
    private static final Set<String> REGISTERED_IDS = new HashSet<>();

    // 🧾 Identity & Metadata
    protected String deviceId;
    protected String name;
    protected String brand;
    protected String model;
    private DeviceType type;

    // 📋 Action & Status
    private List<DeviceAction> actions = new ArrayList<>();
    private boolean isOn;
    private Instant lastOnTimestamp;
    private Instant lastOffTimestamp;

    // ⏰ Timestamps
    private final Clock clock;
    private final ZonedDateTime addedTimestamp;
    private ZonedDateTime updatedTimestamp;
    private ZonedDateTime removedTimestamp;

    // 🤖 AutoOp
    protected boolean automationEnabled = false;
    protected String automationSensorId = null;
    private Sensor linkedSensor;
    private double autoOnThreshold;
    private double autoOffThreshold;
    private boolean isOffThresholdUsed = false; // 🆕 Disable OFF logic unless enabled
    private boolean autoOnUserDefined = false;

    // 🧪 Misc
    private static final int TEST_DURATION_MS = 5_000;

    // 🏗 Constructor
    public Device(String deviceId, String name, DeviceType type, Clock clock,
                  double autoOnThreshold, double autoOffThreshold) {
        if (REGISTERED_IDS.contains(deviceId)) {
            throw new IllegalArgumentException("❌ Device ID already in use: " + deviceId);
        }
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.clock = clock;
        this.autoOnThreshold = autoOnThreshold;
        this.autoOffThreshold = autoOffThreshold;
        this.addedTimestamp = ZonedDateTime.now(clock);
        this.updatedTimestamp = addedTimestamp;
        REGISTERED_IDS.add(deviceId);
    }

    // 🛠️ Identity Setters
    public void setId(String id) {
        if (!Objects.equals(this.deviceId, id)) {
            if (REGISTERED_IDS.contains(id))
                throw new IllegalArgumentException("❌ Duplicate device ID: " + id);
            REGISTERED_IDS.remove(this.deviceId);
            this.deviceId = id;
            REGISTERED_IDS.add(id);
            updateTimestamp();
        }
    }
    public void setName(String name) { this.name = name; updateTimestamp(); }
    public void setType(DeviceType type) { this.type = type; updateTimestamp(); }
    public void setBrand(String brand) { this.brand = brand; updateTimestamp(); }
    public void setModel(String model) { this.model = model; updateTimestamp(); }

    // 🔄 Action Setters
    public void setActions(List<DeviceAction> deviceActions) {
        if (!this.actions.equals(deviceActions)) {
            this.actions = new ArrayList<>(deviceActions);
            updateTimestamp();
        }
    }

    public void setState(String newState) {
        if (newState.equalsIgnoreCase("ON")) {
            this.isOn = true;
            lastOnTimestamp = Instant.now(clock);
        } else if (newState.equalsIgnoreCase("OFF")) {
            this.isOn = false;
            lastOffTimestamp = Instant.now(clock);
        } else {
            throw new IllegalArgumentException("❌ Invalid state: " + newState);
        }
        updateTimestamp();
    }

    // 🌡 Thresholds (AutoOp)
    public void setAutoOnThreshold(double value, boolean userSet) {
        this.autoOnThreshold = value;
        this.autoOnUserDefined = userSet;
    }
    public void setAutoOffThreshold(double value) {
        this.autoOffThreshold = value;
    }
    public void resetAutoOnThreshold() {
        this.autoOnThreshold = DeviceDefaults.getDefaultAutoOn(type);
    }
    public void resetAutoOffThreshold() {
        this.autoOffThreshold = DeviceDefaults.getDefaultAutoOff(type);
    }

    public double getAutoOnThreshold() { return autoOnThreshold; }
    public double getAutoOffThreshold() { return autoOffThreshold; }

    public boolean isOffThresholdUsed() { return isOffThresholdUsed; }
    public void setOffThresholdUsed(boolean enabled) {
        this.isOffThresholdUsed = enabled;
    }

    // 🤖 AutoOp
    public boolean isAutomationEnabled() { return automationEnabled; }
    public void setAutomationEnabled(boolean enabled) { this.automationEnabled = enabled; }
    public String getAutomationSensorId() { return automationSensorId; }
    public void setAutomationSensorId(String id) { this.automationSensorId = id; }

    public void setLinkedSensor(Sensor sensor) {
        this.linkedSensor = sensor;
        this.automationSensorId = (sensor != null) ? sensor.getSensorId() : null;
    }
    public Sensor getLinkedSensor() {
        return linkedSensor;
    }

    // 🔁 State Control
    public boolean isOn() { return isOn; }
    public void turnOn() {
        if (!isOn) {
            isOn = true;
            lastOnTimestamp = Instant.now(clock);
            updateTimestamp();
            System.out.println("⚡ " + name + " turned ON");
            DeviceStorage.getDevices().put(deviceId, this);
            DeviceStorage.updateDeviceState(deviceId, "On");
        }
    }
    public void turnOff() {
        if (isOn) {
            isOn = false;
            lastOffTimestamp = Instant.now(clock);
            updateTimestamp();
            System.out.println("🌙 " + name + " turned OFF");
            DeviceStorage.updateDeviceState(deviceId, "Off");
        }
    }
    public void setOn(boolean isOn) {
        if (this.isOn != isOn) {
            this.isOn = isOn;
            lastOnTimestamp = isOn ? Instant.now(clock) : null;
            updateTimestamp();
            DeviceStorage.getDevices().put(deviceId, this);
            DeviceStorage.updateDeviceState(deviceId, isOn ? "On" : "Off");
        }
    }

    // 🔧 Testing Simulation
    public void testDevice() {
        System.out.println("🔧 Starting test for device: " + getName());
        new Thread(() -> {
            try {
                DeviceStorage.getDevices().put(deviceId, this);
                DeviceStorage.updateDeviceState(deviceId, "On");
                turnOn();
                Thread.sleep(500);
                Device fresh = DeviceStorage.getDevices().get(deviceId);
                if (fresh == null) {
                    System.out.println("❌ Device reference lost in storage!");
                    return;
                }
                System.out.println("🟢 " + name + " is " + fresh.getState());
                Thread.sleep(TEST_DURATION_MS);
                fresh.turnOff();
                DeviceStorage.getDevices().put(deviceId, fresh);
                DeviceStorage.updateDeviceState(deviceId, "Off");
                System.out.println("🔴 " + name + " is OFF");
                System.out.println("✅ Test complete for: " + name);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("⚠️ Test interrupted for " + name);
            }
        }, name + "-TestThread").start();
    }

    public void performAction(String action) {
        System.out.println("🎯 Performing action: " + action + " on " + name);
        simulate(action);
        DeviceStorage.updateDeviceState(getId(), action);
    }

    // 🔍 Getters
    public String getId() { return deviceId; }
    public String getName() { return name; }
    public DeviceType getType() { return type; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getState() { return isOn ? "ON" : "OFF"; }
    public Instant getLastOnTimestamp() { return lastOnTimestamp; }
    public Instant getLastOffTimestamp() { return lastOffTimestamp; }

    // 🕒 Timestamps
    public String getAddedTimestamp() { return addedTimestamp.toString(); }
    public String getUpdatedTimestamp() { return updatedTimestamp.toString(); }
    public String getRemovedTimestamp() {
        return (removedTimestamp != null) ? removedTimestamp.toString() : "N/A";
    }
    public void updateTimestamp() {
        this.updatedTimestamp = ZonedDateTime.now(clock);
    }

    // 🧪 Simulation
    public abstract List<String> getAvailableActions();
    public abstract void simulate(String action);
    public void simulate() {
        System.out.println("💡 Simulating device behavior… Current state: " + getState());
    }

    @Override
    public void run() {
        simulate();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", getType(), getName(), getId(), getState());
    }
    // Accessible to test classes in the same package
    public static void clearDeviceRegistryForTests() {
        REGISTERED_IDS.clear();
    }


}
