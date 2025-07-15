package devices.actions;

public enum SmartLightColorMode {
    WARM_WHITE("Warm White", 255, 244, 229),
    COOL_WHITE("Cool White", 255, 255, 255),
    SUNSET("Sunset", 255, 94, 77),
    FOREST_GREEN("Forest Green", 34, 139, 34),
    OCEAN_BLUE("Ocean Blue", 70, 130, 180),
    CUSTOM("Custom", 0, 0, 0);  // Placeholder, overridden at runtime

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
        for (SmartLightColorMode mode : values()) {
            if (mode.label.equalsIgnoreCase(label)) return mode;
        }
        return CUSTOM;
    }
}
