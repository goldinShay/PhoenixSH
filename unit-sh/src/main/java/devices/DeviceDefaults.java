package devices;

import devices.DeviceType;

public class DeviceDefaults {

    public static double getDefaultAutoOn(DeviceType type) {
        return switch (type) {
            case LIGHT -> 1024.0;
            case THERMOSTAT -> 23.0;
            case DRYER, WASHING_MACHINE -> 0.0; // maybe unused
            default -> 1050.0;
        };
    }

    public static double getDefaultAutoOff(DeviceType type) {
        return switch (type) {
            case LIGHT -> 1050.0;
            case THERMOSTAT -> 27.0;
            case DRYER, WASHING_MACHINE -> 0.0;
            default -> 1080.0;
        };
    }
}
