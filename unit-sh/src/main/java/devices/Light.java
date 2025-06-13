package devices;

import storage.DeviceStorage;
import storage.XlCreator;

import java.time.Clock;
import java.util.*;

public class Light extends Device {

    private int autoOnThreshold;
    private int autoOffThreshold;

    // 🏗️ Constructor: Initialize Light with full parameters
    public Light(String deviceId, String name, Clock clock, boolean isOn, int autoOnThreshold, int autoOffThreshold) {
        super(deviceId, name, DeviceType.LIGHT, clock);
        super.setOn(isOn);  // ✅ Use setter to update device state
        this.autoOnThreshold = autoOnThreshold;
        this.autoOffThreshold = autoOffThreshold;
    }

    // 🏗️ Constructor: Use default auto-thresholds
    public Light(String deviceId, String name, Clock clock, boolean isOn) {
        this(deviceId, name, clock, isOn, 650, 660);  // ✅ Defaults: Evening (650), Morning (660)
    }

    // 🏗️ Constructor: Generate a new ID for Light
    public Light(String name, Clock clock, boolean isOn) {
        this(generateNewId(), name, clock, isOn);
    }

    // 🏗️ Static method: Generate unique ID
    private static String generateNewId() {
        Set<String> allIds = new HashSet<>(DeviceStorage.getDevices().keySet());  // ✅ Get stored IDs
        return XlCreator.getNextAvailableId("LI", allIds);  // ✅ Generate a unique ID
    }

    // 📌 Serialization methods
    @Override
    public String toDataString() {
        return getType() + "|" + getId() + "|" + getName();
    }

    public static Light fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 3) {
            throw new IllegalArgumentException("Invalid data string for Light: " + Arrays.toString(parts));
        }
        return new Light(parts[1], parts[2], clock, false);
    }

    // 🔄 Device-Specific Actions
    @Override
    public List<String> getAvailableActions() {
        return List.of(DeviceAction.ON.name().toLowerCase(), DeviceAction.OFF.name().toLowerCase());
    }

    @Override
    public void simulate(String action) {
        try {
            DeviceAction act = DeviceAction.fromString(action);
            switch (act) {
                case ON -> turnOn();
                case OFF -> turnOff();
                case STATUS -> System.out.println("📊 Light " + getName() + " status: " + (isOn() ? "ON" : "OFF"));
                default -> System.out.println("❓ Unsupported action: " + action);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❓ Unknown action for Light: " + action);
        }
    }

    // 🔄 Auto-Enabler Logic
    public void enableAutoMode() {
        System.out.println("🔄 Auto Mode Enabled for " + getName());
    }

    public void disableAutoMode() {
        System.out.println("🚫 Auto Mode Disabled for " + getName());
    }

    // 🔄 Helper methods
    @Override
    public void turnOn() {
        super.setOn(true);
        System.out.println("💡 Light " + getName() + " turned ON.");
    }

    @Override
    public void turnOff() {
        super.setOn(false);
        System.out.println("💡 Light " + getName() + " turned OFF.");
    }
}
