package devices.actions;

public class SmartLightAction {
    private final String label;
    private final int intensity; // 0–100%
    private final int red;       // 0–255
    private final int green;     // 0–255
    private final int blue;      // 0–255

    // Primary constructor (with label)
    public SmartLightAction(String label, int intensity, int red, int green, int blue) {
        this.label = label;
        this.intensity = clamp(intensity, 0, 100);
        this.red = clamp(red, 0, 255);
        this.green = clamp(green, 0, 255);
        this.blue = clamp(blue, 0, 255);
    }

    // Convenience constructor (no label)
    public SmartLightAction(int intensity, int red, int green, int blue) {
        this("Unnamed Mode", intensity, red, green, blue);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

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

    @Override
    public String toString() {
        return String.format("%s | %d%% brightness | RGB(%d, %d, %d)",
                label, intensity, red, green, blue);
    }
    public SmartLightAction dim() {
        int newIntensity = Math.max(10, (int) (intensity * 0.9));
        return new SmartLightAction(label + " (Dimmed)", newIntensity, red, green, blue);
    }
    public SmartLightAction brighten() {
        int newIntensity = Math.min(100, (int) (intensity * 1.1));
        return new SmartLightAction(label + " (Bright)", newIntensity, red, green, blue);
    }

}
