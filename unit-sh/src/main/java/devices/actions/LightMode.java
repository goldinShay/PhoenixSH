package devices.actions;

public enum LightMode {
    CUSTOM(100, 100, 100),
    FULL_MODE(100, 100, 100),
    WARM_WHITE(100, 90, 80),
    COLD_WHITE(80, 90, 100),
    NIGHT_MODE(60, 50, 40),
    SUNSET(90, 60, 40),
    FOREST(70, 100, 60),
    STEEL_BLUE(60, 90, 100),
    DEEP_PURPLE(80, 60, 100),
    CANDLELIGHT(100, 80, 50);

    private final int r;
    private final int g;
    private final int b;

    LightMode(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }
}

