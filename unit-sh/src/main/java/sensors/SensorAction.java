package sensors;

public enum SensorAction {
    // ⚙️ Basic Actions
    ON,
    OFF,
    READ,
    STATUS,
    SIMULATE,

    // 📷 Advanced Sensor Capabilities
    SNAPSHOT,     // Take a photo
    STREAM,       // Start video stream
    RECORD,       // Begin recording
    SEND_ALERT,   // Notify user/system
    CALIBRATE,    // Adjust baseline

    // 🧪 Diagnostics
    TEST
}
