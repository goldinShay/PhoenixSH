package storage.xlc.sheetsCommand;

public enum XlTabNames {
    DEVICES("Devices"),
    SENSORS("Sensors"),
    SCHEDULED_TASKS("Scheduled Tasks"),
    SENSE_CONTROL("Sense_Control"),
    SMART_LIGHT_CONTROL("Smart_Light_Control");

    private final String label;

    XlTabNames(String label) {
        this.label = label;
    }

    public String value() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
