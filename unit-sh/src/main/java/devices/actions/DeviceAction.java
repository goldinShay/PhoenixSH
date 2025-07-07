package devices.actions;

public enum DeviceAction {
    ON, OFF,
    START, STOP,
    TEMP_UP, TEMP_DOWN,
    VOLUME_UP, VOLUME_DOWN,
    CHANNEL_UP, CHANNEL_DOWN,
    STATUS;

    public static DeviceAction fromString(String input) {
        return switch (input.trim().toUpperCase()) {
            case "ON" -> ON;
            case "OFF" -> OFF;
            case "START" -> START;
            case "STOP" -> STOP;
            case "TEMP+" -> TEMP_UP;
            case "TEMP-" -> TEMP_DOWN;
            case "VOL+" -> VOLUME_UP;
            case "VOL-" -> VOLUME_DOWN;
            case "CH+" -> CHANNEL_UP;
            case "CH-" -> CHANNEL_DOWN;
            default -> throw new IllegalArgumentException("Unknown action: " + input);
        };
    }
}
