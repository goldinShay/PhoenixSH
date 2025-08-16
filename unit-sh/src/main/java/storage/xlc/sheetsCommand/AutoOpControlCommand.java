package storage.xlc.sheetsCommand;

public enum AutoOpControlCommand {
    LINKED_DEVICE_ID,
    LINKED_DEVICE_NAME,
    ACTION,
    THRESHOLD,
    CRNT_VAL,
    SENSOR_NAME,
    SENSOR_ID,
    UPDATED_TS;

    public String label() {
        return name();
    }
}
