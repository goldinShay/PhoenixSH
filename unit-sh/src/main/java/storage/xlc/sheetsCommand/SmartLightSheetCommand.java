package storage.xlc.sheetsCommand;

public enum SmartLightSheetCommand {
    DEVICE_ID("DeviceID"),
    RED("Red"),
    GREEN("Green"),
    BLUE("Blue"),
    ACTIVE_MODE("ActiveMode"),
    LAST_UPDATED("LastUpdated");

    private final String label;

    SmartLightSheetCommand(String label) {
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
