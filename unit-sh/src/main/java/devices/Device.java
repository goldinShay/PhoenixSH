package devices;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

public abstract class Device implements Runnable {

    // 🧱 Enforce uniqueness
    private static final Set<String> REGISTERED_IDS = new HashSet<>();

    // 🔑 Identity & Core Properties
    protected String deviceId;
    protected String name;
    private DeviceType type;

    // 🏷️ Optional Metadata
    protected String brand;
    protected String model;

    // ✅ Enum-based Actions
    private List<DeviceAction> actions = new ArrayList<>();

    // 🔄 Status
    private boolean isOn;
    private Instant lastOnTimestamp;
    private Instant lastOffTimestamp;

    // 🕒 Timestamps
    private final Clock clock;
    private final ZonedDateTime addedTimestamp;  // ✅ Tracks when device was first created
    private ZonedDateTime updatedTimestamp;  // ✅ Updates when device is modified
    private ZonedDateTime removedTimestamp;  // ✅ Marks removal time

    private static final int TEST_DURATION_MS = 5_000;

    // ✅ Constructor with unique ID enforcement
    public Device(String deviceId, String name, DeviceType type, Clock clock) {
        if (REGISTERED_IDS.contains(deviceId)) {
            throw new IllegalArgumentException("❌ Device ID already in use: " + deviceId);
        }

        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.clock = clock;
        this.addedTimestamp = ZonedDateTime.now(clock); // ✅ Ensure timestamp is set on creation
        this.updatedTimestamp = addedTimestamp; // ✅ First update matches creation time

        REGISTERED_IDS.add(deviceId);
        System.out.println("🛠️ Debug - Device Created: " + this.name + " at " + this.addedTimestamp); // 🔥 Debugging print
    }


    // 🔓 Public Setters (Enforce ID uniqueness)
    public void setId(String id) {
        if (Objects.equals(this.deviceId, id)) return;

        if (REGISTERED_IDS.contains(id)) {
            throw new IllegalArgumentException("❌ Cannot assign duplicate device ID: " + id);
        }

        REGISTERED_IDS.remove(this.deviceId);
        this.deviceId = id;
        REGISTERED_IDS.add(id);
        updateTimestamp();  // ✅ Timestamp update for ID change
    }

    public void setName(String name) {
        this.name = name;
        updateTimestamp();  // ✅ Timestamp update for name change
    }

    public void setType(DeviceType type) {
        this.type = type;
        updateTimestamp();  // ✅ Timestamp update for type change
    }

    public void setBrand(String brand) {
        this.brand = brand;
        updateTimestamp();  // ✅ Timestamp update for brand change
    }

    public void setModel(String model) {
        this.model = model;
        updateTimestamp();  // ✅ Timestamp update for model change
    }

    public List<DeviceAction> getActions() {
        return new ArrayList<>(actions);
    }

    public void setActions(List<DeviceAction> deviceActions) {
        if (!this.actions.equals(deviceActions)) {
            this.actions = new ArrayList<>(deviceActions);
            updateTimestamp();  // ✅ Timestamp update for action change
        }
    }

    // 🌟 New Methods for Timestamp Tracking
    public String getAddedTimestamp() {
        return addedTimestamp != null ? addedTimestamp.toString() : "N/A"; // ✅ Ensures it's always a valid string
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp != null ? updatedTimestamp.toString() : "N/A";
    }

    public String getRemovedTimestamp() {
        return removedTimestamp != null ? removedTimestamp.toString() : "N/A";
    }

    public void updateTimestamp() {
        this.updatedTimestamp = ZonedDateTime.now(clock);
    }

    public void markAsRemoved() {
        this.removedTimestamp = ZonedDateTime.now(clock);
    }

    public String getId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public DeviceType getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getState() {
        return isOn ? "On" : "Off";
    }

    public boolean isOn() {
        return isOn;
    }

    public Instant getLastOnTimestamp() {
        return lastOnTimestamp;
    }

    public Instant getLastOffTimestamp() {
        return lastOffTimestamp;
    }

    public void turnOn() {
        if (!isOn) {
            isOn = true;
            lastOnTimestamp = Instant.now(clock);
            updateTimestamp(); // ✅ Mark as updated when turned ON
        }
    }

    public void turnOff() {
        if (isOn) {
            isOn = false;
            lastOffTimestamp = Instant.now(clock);
            updateTimestamp(); // ✅ Mark as updated when turned OFF
        }
    }

    public void setOn(boolean on) {
        if (this.isOn != on) {
            if (on) turnOn();
            else turnOff();
        }
    }

    public void testDevice() {
        System.out.println("🔧 Starting test for device: " + getName());

        Thread testThread = new Thread(() -> {
            try {
                turnOn();
                System.out.println("🟢 " + getName() + " is ON");
                Thread.sleep(TEST_DURATION_MS);
                turnOff();
                System.out.println("🔴 " + getName() + " is OFF");
                System.out.println("✅ Test complete for: " + getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("⚠️ Test interrupted for " + getName());
            }
        }, getName() + "-TestThread");

        testThread.start();
    }

    public void performAction(String action) {
        System.out.println("🎯 Performing action: " + action + " on " + getName());
        simulate(action);
    }

    public abstract List<String> getAvailableActions();
    public abstract void simulate(String action);
    public abstract void simulate();
    public abstract String toDataString();

    @Override
    public void run() {
        simulate();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", getType(), getName(), getId(), isOn() ? "On" : "Off");
    }

    // 🧹 Resets registry (for tests or full reload)
    public static void resetRegisteredIds() {
        REGISTERED_IDS.clear();
    }
}
