import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

// Base class for all devices
public abstract class Device implements Runnable {
    protected String deviceId;
    protected String name;
    protected String type;
    private boolean isOn;
    private Instant lastOnTimestamp;
    private Instant lastOffTimestamp;


    private final Clock clock;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime removedAt;

    public Device(String deviceId, String name, String type, Clock clock) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.clock = clock;
        this.createdAt = ZonedDateTime.now(clock);
        this.updatedAt = createdAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public String getType() {
        return type;
    }

    public Instant getLastOnTimestamp() {
        return lastOnTimestamp;
    }

    public Instant getLastOffTimestamp() {
        return lastOffTimestamp;
    }


    public void setType(String type) {
        this.type = type;
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void turnOn() {
        isOn = true;
        lastOnTimestamp = Instant.now(clock);
        this.updatedAt = ZonedDateTime.now(clock);
    }

    public void turnOff() {
        isOn = false;
        lastOffTimestamp = Instant.now(clock);
        this.updatedAt = ZonedDateTime.now(clock);
    }


    public boolean isOn() {
        return isOn;
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

    public void markAsRemoved(Clock clock) {
        this.removedAt = ZonedDateTime.now(clock);
    }


    public abstract void simulate();

    @Override
    public void run() {
        simulate();
    }

    public abstract String toDataString(); // Abstract method for persistence
}
