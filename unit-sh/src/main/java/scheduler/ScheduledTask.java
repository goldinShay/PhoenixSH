package scheduler;

import devices.Device;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a task scheduled to perform an action on a device at a specific time,
 * with optional repeat intervals (e.g., daily, weekly, monthly, or none).
 */
public class ScheduledTask {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    private Device device;
    private String action;
    private LocalDateTime time;
    private String repeat; // Options: "daily", "weekly", "monthly", "none" (case-insensitive)

    public ScheduledTask(Device device, String action, LocalDateTime time, String repeat) {
        this.device = device;
        this.action = action;
        this.time = time;
        this.repeat = repeat != null ? repeat.toLowerCase() : "none";
    }

    public static void scheduleDevicePower() {
    }

    // Getters
    public Device getDevice() {
        return device;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getRepeat() {
        return repeat;
    }

    // Setters
    public void setDevice(Device device) {
        this.device = device;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat != null ? repeat.toLowerCase() : "none";
    }

    /**
     * Returns a human-readable summary for printing in logs.
     */
    @Override
    public String toString() {
        return "[" + time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] "
                + device.getName() + " - " + action + " (Repeat: " + repeat + ")";
    }

    /**
     * Returns a file-friendly version (e.g., for saving to text file).
     */
    public String toFileString() {
        return device.getId() + "|" + device.getName() + "|" + action + "|" +
                time.format(FORMATTER) + "|" + repeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledTask)) return false;
        ScheduledTask that = (ScheduledTask) o;
        return Objects.equals(device.getId(), that.device.getId()) &&
                Objects.equals(action, that.action) &&
                Objects.equals(time, that.time) &&
                Objects.equals(repeat, that.repeat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device.getId(), action, time, repeat);
    }
}
