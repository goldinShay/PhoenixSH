package storage.xlc.sheetsCommand;

public enum XlTabNames {
    SCHEDULED_TASKS("Scheduled Tasks"),
    DEVICES("Devices"),
    SMART_LIGHT_CONTROL("Smart_Light_Control"),
    SENSORS("Sensors"),
    SENSE_CONTROL("Sense_Control");

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
