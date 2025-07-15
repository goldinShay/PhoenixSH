package scheduler;

import devices.Device;
import storage.xlc.sheetsCommand.ScheduledTasksCommand;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledTask {
    private List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    private String taskId;
    private Device device;
    private String action;
    private LocalDateTime time;
    private String repeat; // "daily", "weekly", etc.

    public ScheduledTask(String taskId, Device device, String action, LocalDateTime time, String repeat) {
        this.taskId = taskId;
        this.device = device;
        this.action = action;
        this.time = time;
        this.repeat = repeat != null ? repeat.toLowerCase() : "none";
    }

    public ScheduledTask(Device device, String action, LocalDateTime time, String repeat) {
        this("TS-UNASSIGNED", device, action, time, repeat);
    }


    // Getters
    public String getTaskId() {
        return taskId;
    }

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
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

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
     * Convert to row representation using enum-backed column order
     */
    public String[] toExcelRow() {
        return new String[] {
                taskId,
                device.getId(),
                action,
                FORMATTER.format(time),
                repeat
        };
    }

    @Override
    public String toString() {
        return "[" + time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] "
                + device.getName() + " - " + action + " (Repeat: " + repeat + ")";
    }

    public String toFileString() {
        return taskId + "|" + device.getId() + "|" + action + "|" + FORMATTER.format(time) + "|" + repeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledTask)) return false;
        ScheduledTask that = (ScheduledTask) o;
        return Objects.equals(taskId, that.taskId)
                && Objects.equals(device.getId(), that.device.getId())
                && Objects.equals(action, that.action)
                && Objects.equals(time, that.time)
                && Objects.equals(repeat, that.repeat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, device.getId(), action, time, repeat);
    }
    public Set<String> getExistingTaskIds() {
        return scheduledTasks.stream()
                .map(ScheduledTask::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
