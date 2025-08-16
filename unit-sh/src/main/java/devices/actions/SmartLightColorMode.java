package devices.actions;

import utils.Log;

public enum SmartLightColorMode {
    WARM_WHITE("Warm White", 255, 169, 169),
    COOL_WHITE("Cool White", 255, 255, 255),
    SUNSET("Sunset", 255, 94, 77),
    NIGHT_MODE("Night Mode",200, 128, 100),
    DEEP_PURPLE("Deep Purple",200, 145, 255),
    CANDLELIGHT("Candlelight",255, 215, 128),
    FOREST_GREEN("Forest Green", 34, 139, 34),
    OCEAN_BLUE("Ocean Blue", 70, 130, 180),
    CUSTOM("Custom", 100, 169, 10);  // Placeholder, overridden at runtime

    private final String label;
    private final int r, g, b;

    SmartLightColorMode(String label, int r, int g, int b) {
        this.label = label;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public String getLabel() { return label; }
    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }

    public boolean isCustom() { return this == CUSTOM; }

    public static SmartLightColorMode fromLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            Log.warn("⚠️ Colour mode label missing — defaulting to WARM_WHITE");
            return SmartLightColorMode.WARM_WHITE;
        }

        for (SmartLightColorMode mode : values()) {
            if (mode.getLabel().equalsIgnoreCase(label.trim())) {
                return mode;
            }
        }

        Log.warn("⚠️ Unknown colour mode: " + label + " — defaulting to WARM_WHITE");
        return SmartLightColorMode.WARM_WHITE;
    }
    public static SmartLightColorMode matchColorMode(int r, int g, int b) {
        for (SmartLightColorMode mode : SmartLightColorMode.values()) {
            if (mode.getRed() == r && mode.getGreen() == g && mode.getBlue() == b) {
                return mode;
            }
        }
        return SmartLightColorMode.CUSTOM;
    }

}
