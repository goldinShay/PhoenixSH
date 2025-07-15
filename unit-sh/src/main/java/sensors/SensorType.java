package sensors;

public enum SensorType {
    // 🌞 Environmental
    LIGHT("Lux", 1500.0),
    TEMPERATURE("°C", 22.0),
    HUMIDITY("%", 45.0),
    AIR_QUALITY("AQI", 42.0),
    UV("Index", 3.0),

    // 💧 Utility & Resource
    WATER_LEVEL("cm", 75.0),
    MOISTURE("%", 60.0),
    GAS_LEAK("ppm", 0.0),
    POWER_USAGE("kWh", 1.5),
    DETERGENT_LEVEL("%", 80.0),
    SOFTENER_LEVEL("%", 80.0),

    // 🏠 Security & Access
    MOTION("Boolean", 0.0),
    CONTACT("Boolean", 0.0),
    GLASS_BREAK("Boolean", 0.0),
    RFID_READER("Code", 0.0),

    // 📷 Visual & Media
    CAMERA("Visual", 0.0),
    VIDEO_DOORBELL("Visual", 0.0),

    // ⚙️ Infrastructure / System
    PRESSURE("psi", 30.0),
    DISTANCE("cm", 120.0),
    VIBRATION("Hz", 0.0),
    SOUND_LEVEL("dB", 40.0),

    // 🧪 Diagnostic or Virtual
    VIRTUAL("Unitless", 0.0),
    DEBUG("Unitless", 0.0),
    CALIBRATION("Unitless", 0.0);

    private final String defaultUnit;
    private final double defaultValue;

    SensorType(String defaultUnit, double defaultValue) {
        this.defaultUnit = defaultUnit;
        this.defaultValue = defaultValue;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public double getDefaultValue() {
        return defaultValue;
    }
}
