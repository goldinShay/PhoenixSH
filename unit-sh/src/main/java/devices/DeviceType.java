package devices;

public enum DeviceType {
    UNKNOWN("Unknown"),
    LIGHT("Light"),
    SMART_LIGHT("Smart Light"),
    THERMOSTAT("Thermostat"),
    WASHING_MACHINE("Washing Machine"),
    DRYER("Dryer"),
    SENSOR("Sensor");

    private final String label;

    DeviceType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
    public static DeviceType fromString(String input) {
        if (input == null || input.isBlank()) return UNKNOWN;

        String normalized = input.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        return switch (normalized) {
            case "LIGHT" -> LIGHT;
            case "SMART_LIGHT" -> SMART_LIGHT;
            case "THERMOSTAT" -> THERMOSTAT;
            case "DRYER" -> DRYER;
            case "WASHING_MACHINE" -> WASHING_MACHINE;
            case "SENSOR" -> SENSOR;
            default -> UNKNOWN;
        };
    }


}

