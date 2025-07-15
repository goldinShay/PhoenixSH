package storage.xlc.sheetsCommand;

public enum SenseControlCommand {
    SENSOR_ID,
    SLAVE_DEVICE_ID;

    public String label() {
        return name();
    }
}
