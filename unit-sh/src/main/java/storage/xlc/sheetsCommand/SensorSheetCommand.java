package storage.xlc.sheetsCommand;

public enum SensorSheetCommand {
    SENSOR_ID,
    NAME,
    TYPE,
    CURRENT_VALUE;

    public String label() {
        return name();
    }
}
