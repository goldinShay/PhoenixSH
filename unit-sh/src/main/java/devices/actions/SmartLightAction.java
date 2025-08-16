package devices.actions;

public class SmartLightAction {
    private final String label;
    private final int intensity; // 0–100%
    private final int red;       // 0–255
    private final int green;     // 0–255
    private final int blue;      // 0–255
    private final SmartLightColorMode colorMode;
    private final SmartLightEffect fxMode;


    // ───────────────────── Constructors ─────────────────────

    public SmartLightAction(String label, int intensity, int red, int green, int blue,
                            SmartLightColorMode colorMode, SmartLightEffect fxMode) {
        this.label = label;
        this.intensity = clamp(intensity, 0, 100);
        this.red = clamp(red, 0, 255);
        this.green = clamp(green, 0, 255);
        this.blue = clamp(blue, 0, 255);
        this.colorMode = colorMode;
        this.fxMode = fxMode;
    }


    public SmartLightAction(int intensity, int red, int green, int blue) {
        this("NONE", intensity, red, green, blue, SmartLightColorMode.CUSTOM, SmartLightEffect.NONE);
    }

    // ───────────────────── Utility Methods ─────────────────────

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    // ───────────────────── Accessors ─────────────────────

    public String getLabel() {
        return label;
    }

    public int getIntensity() {
        return intensity;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public SmartLightColorMode getColorMode() {
        return colorMode;
    }
    public SmartLightEffect getFxMode() {
        return fxMode;
    }

    // ───────────────────── Transformations ─────────────────────

    public SmartLightAction dim() {
        int newIntensity = Math.max(10, (int) (intensity * 0.9));
        return new SmartLightAction(label + " (Dimmed)", newIntensity, red, green, blue, colorMode, fxMode);
    }

    public SmartLightAction brighten() {
        int newIntensity = Math.min(100, (int) (intensity * 1.1));
        return new SmartLightAction(label + " (Bright)", newIntensity, red, green, blue, colorMode, fxMode);
    }

    // ───────────────────── Debug ─────────────────────

    @Override
    public String toString() {
        return String.format("%s | %d%% brightness | RGB(%d, %d, %d) | Mode: %s | FX: %s",
                label, intensity, red, green, blue, colorMode.getLabel(), fxMode.name());
    }
}
