package devices;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Device implements Runnable {

    // 🔑 Identity & Core Properties
    protected String deviceId;
    protected String name;
    protected String type;

    // 🏷️ Optional Metadata (Excel + SHS friendly)
    protected String brand;
    protected String model;
    protected List<String> actions = new ArrayList<>();

    // 🔄 Status
    private boolean isOn;
    private Instant lastOnTimestamp;
    private Instant lastOffTimestamp;

    // 🕒 Timestamps
    private final Clock clock;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime removedAt;
    private static final int TEST_DURATION_MS = 10_000;

    // ✅ Constructor
    public Device(String deviceId, String name, String type, Clock clock) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.clock = clock;
        this.createdAt = ZonedDateTime.now(clock);
        this.updatedAt = createdAt;
    }

    // 🔓 Public Setters (used by ExcelDeviceLoader)
    public void setId(String id) {
        this.deviceId = id;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void setType(String type) {
        this.type = type;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void setBrand(String brand) {
        this.brand = brand;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void setModel(String model) {
        this.model = model;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void setActions(List<String> actions) {
        if (!this.actions.equals(actions)) {
            this.actions = new ArrayList<>(actions);
            this.updatedAt = ZonedDateTime.now(clock);
        }
    }


    // 🔒 Getters
    public String getId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public List<String> getActions() {
        return new ArrayList<>(actions);
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public ZonedDateTime getRemovedAt() {
        return removedAt;
    }

    // ⚙️ State Changers
    public void turnOn() {
        if (!isOn) {
            isOn = true;
            lastOnTimestamp = Instant.now(clock);
            updatedAt = ZonedDateTime.now(clock);
        }
    }

    public void turnOff() {
        if (isOn) {
            isOn = false;
            lastOffTimestamp = Instant.now(clock);
            updatedAt = ZonedDateTime.now(clock);
        }
    }


    public void markAsRemoved() {
        this.removedAt = ZonedDateTime.now(clock);
    }

    public void setOn(boolean on) {
        if (this.isOn != on) {
            if (on) turnOn();
            else turnOff();
        }
    }


    // 🧪 Testing Helper
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

    // ⏰ SHS Invoker
    public void performAction(String action) {
        System.out.println("🎯 Performing action: " + action + " on " + getName());
        simulate(action);
    }

    // 🔌 Simulation Layer (implemented by subclasses)
    public abstract List<String> getAvailableActions();

    public abstract void simulate(String action);

    public abstract void simulate();

    public abstract String toDataString();

    @Override
    public void run() {
        simulate();
    }
}
