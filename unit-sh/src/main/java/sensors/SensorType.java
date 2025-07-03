package sensors;

public enum SensorType {
    // 🌞 Environmental
    LIGHT,
    TEMPERATURE,
    HUMIDITY,
    AIR_QUALITY,
    UV,

    // 💧 Utility & Resource
    WATER_LEVEL,
    MOISTURE,
    GAS_LEAK,
    POWER_USAGE,
    SOFTENER_LEVEL,

    // 🏠 Security & Access
    MOTION,
    CONTACT,         // e.g., door/window open sensors
    GLASS_BREAK,
    RFID_READER,

    // 📷 Visual & Media
    CAMERA,
    VIDEO_DOORBELL,

    // ⚙️ Infrastructure / System
    PRESSURE,
    DISTANCE,
    VIBRATION,
    SOUND_LEVEL,

    // 🧪 Diagnostic or Virtual
    VIRTUAL,
    DEBUG,
    CALIBRATION
}

