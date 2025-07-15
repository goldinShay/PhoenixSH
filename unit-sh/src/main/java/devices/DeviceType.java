package devices;

import utils.Log;

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
    public static DeviceType fromString(String typeString) {
        if (typeString == null || typeString.isBlank()) return DeviceType.UNKNOWN;

        try {
            return DeviceType.valueOf(typeString.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            Log.warn("🚫 Unknown DeviceType received: '" + typeString + "'");
            return DeviceType.UNKNOWN;
        }
    }

}

