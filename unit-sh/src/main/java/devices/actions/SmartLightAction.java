package devices.actions;

public class SmartLightAction {
    private final int intensity;   // 0–100%
    private final int red;         // 0–255
    private final int green;       // 0–255
    private final int blue;        // 0–255

    public SmartLightAction(int intensity, int red, int green, int blue) {
        this.intensity = clamp(intensity, 0, 100);
        this.red = clamp(red, 0, 255);
        this.green = clamp(green, 0, 255);
        this.blue = clamp(blue, 0, 255);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
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
        return String.format("%d%% brightness | RGB(%d, %d, %d)", intensity, red, green, blue);
    }
}
