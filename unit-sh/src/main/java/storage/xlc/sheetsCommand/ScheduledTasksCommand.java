package storage.xlc.sheetsCommand;

public enum ScheduledTasksCommand {
    TASK_ID("TaskID"),
    DEVICE_ID("DeviceID"),
    ACTION("Action"),
    TIME("Time"),
    REPEAT("Repeat"),      // Optional if you support recurring tasks
    STATUS("Status"),      // Optional for done/pending
    PRIORITY("Priority");  // Optional if you rank tasks

    private final String label;

    ScheduledTasksCommand(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
