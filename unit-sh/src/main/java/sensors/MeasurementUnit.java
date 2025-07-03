package sensors;

public enum MeasurementUnit {
    LUX("lux"),
    CELSIUS("°C"),
    PERCENT("%"),
    PPM("ppm"),
    DB("dB"),
    ON_OFF("ON/OFF"),
    LITERS("liters"),
    PASCAL("Pa"),
    MOISTURE("%"),
    UNKNOWN("N/A");

    private final String display;

    MeasurementUnit(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return display;
    }

    // Optional: convert from string input
    public static MeasurementUnit fromString(String input) {
        for (MeasurementUnit unit : values()) {
            if (unit.display.equalsIgnoreCase(input) || unit.name().equalsIgnoreCase(input)) {
                return unit;
            }
        }
        return UNKNOWN;
    }
}

