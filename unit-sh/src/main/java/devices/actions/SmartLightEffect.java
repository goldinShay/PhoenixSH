package devices.actions;

/**
 * ✨ SmartLightEffect — describes visual animation patterns for SmartLight devices.
 * Each effect is a predefined dynamic lighting mode that may alter brightness, hue,
 * or rhythm over time.
 */
public enum SmartLightEffect {
    /**
     * 🌈 Cycles through a full-spectrum rainbow gradient endlessly.
     */
    RAINBOW("Full-Spectrum Color Cycle"),

    /**
     * 🔥 Simulates candle/firelight flickering using warm hues and subtle randomness.
     */
    FIRE("Warm Flicker"),

    /**
     * 🌅 Gradually increases brightness from dark to light, imitating sunrise.
     */
    SUNRISE_FADE("Sunrise Transition"),

    /**
     * 💨 Pulses brightness in and out smoothly, like breathing.
     */
    BREATHING("Slow Breathing Light"),

    /**
     * 🎉 Loops quickly through bold colors for party ambience.
     */
    PARTY_LOOP("High-Energy Color Jumps"),

    /**
     * ❎ No animation; static light mode.
     */
    NONE("No Effect");

    private final String description;

    SmartLightEffect(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + " — " + description;
    }
    public static SmartLightEffect fromLabel(String label) {
        for (SmartLightEffect effect : values()) {
            if (effect.name().equalsIgnoreCase(label)) {
                return effect;
            }
        }
        return SmartLightEffect.NONE; // Fallback if label not recognized
    }

}
