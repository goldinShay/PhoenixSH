package sensors;

import java.util.HashMap;
import java.util.Map;

public enum MeasurementUnit {
    NONE("NONE"),
    BOOLEAN("TRUE/FALSE"),
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

    // 🔁 Alias map for flexible parsing
    private static final Map<String, MeasurementUnit> ALIASES = new HashMap<>();

    static {
        // CELSIUS aliases
        ALIASES.put("°C", CELSIUS);
        ALIASES.put("C", CELSIUS);
        ALIASES.put("CELSIUS", CELSIUS);
        ALIASES.put("DEGREES", CELSIUS);
        ALIASES.put("DEGREES CELSIUS", CELSIUS);

        // BOOLEAN aliases
        ALIASES.put("TRUE/FALSE", BOOLEAN);
        ALIASES.put("BOOLEAN", BOOLEAN);

        // ON_OFF aliases
        ALIASES.put("ON/OFF", ON_OFF);

        // NONE & UNKNOWN
        ALIASES.put("NONE", NONE);
        ALIASES.put("N/A", UNKNOWN);
        ALIASES.put("UNKNOWN", UNKNOWN);

        // Other direct matches (optional redundancy)
        ALIASES.put("LUX", LUX);
        ALIASES.put("PPM", PPM);
        ALIASES.put("DB", DB);
        ALIASES.put("LITERS", LITERS);
        ALIASES.put("PA", PASCAL);
        ALIASES.put("%", PERCENT); // Prefer PERCENT to MOISTURE
    }

    public static MeasurementUnit fromString(String input) {
        if (input == null || input.isBlank()) return UNKNOWN;

        String normalized = input.trim().toUpperCase();
        MeasurementUnit aliasMatch = ALIASES.get(normalized);
        if (aliasMatch != null) return aliasMatch;

        for (MeasurementUnit unit : values()) {
            if (unit.getDisplay().equalsIgnoreCase(input) || unit.name().equalsIgnoreCase(normalized)) {
                return unit;
            }
        }

        return UNKNOWN;
    }
}
