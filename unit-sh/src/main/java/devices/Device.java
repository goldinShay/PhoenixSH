package devices;

import devices.actions.DeviceAction;
import sensors.Sensor;
import storage.DeviceStorage;

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
    // ✅ Identity fields
    protected String brand;
    protected String model;

    // ✅ Public accessors for brand/model used in device factories
    public String brand() { return brand; }
    public String model() { return model; }

    private DeviceType type;

    // 📋 Action & Status
    private List<DeviceAction> actions = new ArrayList<>();
    public List<DeviceAction> supportedActions;
    private boolean state = false; // default OFF

    private boolean isOn;
    private Instant lastOnTimestamp;
    private Instant lastOffTimestamp;

    // ⏰ Timestamps
    private final Clock clock;
    private ZonedDateTime addedTimestamp;
    private ZonedDateTime updatedTimestamp;
    private ZonedDateTime removedTimestamp;

    // 🤖 AutoOp
    protected boolean automationEnabled = false;
    protected String automationSensorId = null;
    private Sensor linkedSensor;
    private double autoOnThreshold;
    private double autoOffThreshold;
    private boolean isOffThresholdUsed = false;
    private boolean autoOnUserDefined = false;

    // 🧪 Misc
    private static final int TEST_DURATION_MS = 5_000;

    // 🏗 Constructor
    public Device(String deviceId, String name, DeviceType type, Clock clock,
                  double autoOnThreshold, double autoOffThreshold, boolean skipIdCheck) {

        if (!skipIdCheck && REGISTERED_IDS.contains(deviceId)) {
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

    // 🛠️ Setters – Identity
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

    // 🕒 Timestamp Setters
    public void setAddedTimestamp(ZonedDateTime ts) { this.addedTimestamp = ts; }
    public void setUpdatedTimestamp(ZonedDateTime ts) { this.updatedTimestamp = ts; }
    public void setRemovedTimestamp(ZonedDateTime ts) { this.removedTimestamp = ts; }
    public void updateTimestamp() { this.updatedTimestamp = ZonedDateTime.now(clock); }

    // ⚙️ Action and Behavior
    public void setActions(List<DeviceAction> deviceActions) {
        if (!this.actions.equals(deviceActions)) {
            this.actions = new ArrayList<>(deviceActions);
            updateTimestamp();
        }
    }

    public void setState(String newState) {
        if (newState.equalsIgnoreCase("ON")) {
            isOn = true;
            lastOnTimestamp = Instant.now(clock);
        } else if (newState.equalsIgnoreCase("OFF")) {
            isOn = false;
            lastOffTimestamp = Instant.now(clock);
        } else {
            throw new IllegalArgumentException("❌ Invalid state: " + newState);
        }
        updateTimestamp();
    }
    public void setState(boolean on) {
        this.state = on;
    }
    public void toggleState() {
        this.state = !this.state;
    }


    public void performAction(String action) {
        System.out.println("🎯 Performing action: " + action + " on " + name);
        simulate(action);
        DeviceStorage.updateDeviceState(getId(), action);
    }

    // 🔁 State Control
    public boolean isOn() {
        return state;
    }
    public String getState() { return isOn ? "ON" : "OFF"; }

    public void setOn(boolean isOn) {
        if (this.state != isOn) {
            this.state = isOn;

            if (isOn) {
                lastOnTimestamp = Instant.now(clock);
            } else {
                lastOffTimestamp = Instant.now(clock);
            }

            updateTimestamp();
            DeviceStorage.getDevices().put(deviceId, this);
            DeviceStorage.updateDeviceState(deviceId, isOn ? "ON" : "OFF");
        }
    }

    public void turnOn() {
        if (!state) {
            setOn(true);
            System.out.println("⚡ " + name + " turned ON");
        }
    }

    public void turnOff() {
        if (state) {
            setOn(false);
            System.out.println("🌙 " + name + " turned OFF");
        }
    }


    // 🧠 AutoOp
    // ⚙️ Automation Controls

    public boolean isAutomationEnabled() {
        return automationEnabled;
    }

    public void setAutomationEnabled(boolean enabled) {
        this.automationEnabled = enabled;
    }

    // 🎚️ Threshold Handling (AutoOn used for both directions now)

    public void setAutoThreshold(double value, boolean userSet) {
        this.autoOnThreshold = value;
        this.autoOnUserDefined = userSet;
    }

    public void resetAutoThreshold() {
        this.autoOnThreshold = DeviceDefaults.getDefaultAutoOn(type);
    }

    public double getAutoThreshold() {
        return autoOnThreshold;
    }

    // 🔗 Sensor Linking
    public String getAutomationSensorId() { return automationSensorId; }
    public void setAutomationSensorId(String id) { this.automationSensorId = id; }
    public void setLinkedSensor(Sensor sensor) {
        this.linkedSensor = sensor;
        this.automationSensorId = (sensor != null) ? sensor.getSensorId() : null;
    }


    public void enableAutoMode() {
        System.out.println("🔄 AutoOpi Mode Enabled for " + name);
    }

    public void disableAutoMode() {
        System.out.println("🚫 Auto Mode Disabled for " + name);
    }

    // 🧪 Simulation
    public void simulate() {
        System.out.println("💡 Simulating device behavior… Current state: " + getState());
    }

    public abstract void simulate(String action);
    public abstract List<String> getAvailableActions();

    public void testDevice() {
        System.out.println("🔧 Starting test for device: " + getName());
        new Thread(() -> {
            try {
                turnOn();
                Thread.sleep(TEST_DURATION_MS);
                turnOff();
                System.out.println("✅ Test complete for: " + name);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("⚠️ Test interrupted for " + name);
            }
        }, name + "-TestThread").start();
    }

    // 📤 Getters
    public String getId() { return deviceId; }
    public String getName() { return name; }
    public DeviceType getType() { return type; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Instant getLastOnTimestamp() { return lastOnTimestamp; }
    public Instant getLastOffTimestamp() { return lastOffTimestamp; }

    public String getAddedTimestamp() {
        return (addedTimestamp != null) ? addedTimestamp.toString() : "N/A";
    }

    public String getUpdatedTimestamp() {
        return (updatedTimestamp != null) ? updatedTimestamp.toString() : "N/A";
    }

    public String getRemovedTimestamp() {
        return (removedTimestamp != null) ? removedTimestamp.toString() : "N/A";
    }

    public String getSupportedActionsAsText() {
        if (supportedActions == null || supportedActions.isEmpty()) {
            return "";
        }
        return supportedActions.stream()
                .map(Enum::name)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    // 🧼 Misc
    public static void clearDeviceRegistryForTests() {
        REGISTERED_IDS.clear();
    }

    @Override
    public void run() {
        simulate();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", getType(), getName(), getId(), getState());
    }

    public Sensor getLinkedSensor() {
        return linkedSensor;
    }
    // 🕰️ Clock Accessor
    public Clock getClock() {
        return clock;
    }


}