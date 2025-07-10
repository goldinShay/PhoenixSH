package devices;

public enum DeviceType {

    UNKNOWN,
    LIGHT,
    SMART_LIGHT,
    THERMOSTAT,
    WASHING_MACHINE,
    DRYER,
    SENSOR;

    public static DeviceType fromString(String input) {
        try {
            return DeviceType.valueOf(input.trim().toUpperCase());  // 🔥 Guarantees uppercase
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("❌ Unsupported device type: " + input);
        }
    }
    // ✅ Checks if a string matches any valid DeviceType
    public static boolean isValidType(String type) {
        for (DeviceType dt : values()) {
            if (dt.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }




    @Override
    public String toString() {
        return name();
    }
}
