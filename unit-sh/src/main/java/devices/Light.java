package devices;

import java.time.Clock;
import java.util.List;

public class Light extends Device {

    // 🏗️ Constructor: full parameter set
    public Light(String deviceId, String name, Clock clock, boolean isOn,
                 double autoOnThreshold, double autoOffThreshold, boolean skipIdCheck) {
        super(deviceId, name, DeviceType.LIGHT, clock, autoOnThreshold, autoOffThreshold, skipIdCheck);
        setOn(isOn);
    }


    // 🏗️ Constructor: uses default thresholds
    public Light(String deviceId, String name, Clock clock, boolean isOn) {
        super(deviceId, name, DeviceType.LIGHT, clock,
                DeviceDefaults.getDefaultAutoOn(DeviceType.LIGHT),
                DeviceDefaults.getDefaultAutoOn(DeviceType.LIGHT), false); // strict mode
        setOn(isOn);
    }


    // 📝 Lightweight serialization
    public String toDataString() {
        return getType() + "|" + getId() + "|" + getName();
    }

    public static Light fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 3) {
            throw new IllegalArgumentException("Invalid data string for Light: " + String.join(", ", parts));
        }
        return new Light(parts[1], parts[2], clock, false);
    }

    // 🔁 Available device actions
    @Override
    public List<String> getAvailableActions() {
        return List.of("on", "off");
    }

    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "on" -> turnOn();
            case "off" -> turnOff();
            case "status" -> System.out.printf("📊 Light %s status: %s%n", getName(), getState());
            default -> System.out.printf("❓ Unknown action: '%s'%n", action);
        }
    }
    public void status() {
        System.out.printf("🔍 %s | %s | State: %s | AutoOp: %b%n",
                getType(), getName(), getState(), isAutomationEnabled());
    }

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

    // 🟢 Optional markers for AutoOp toggling (logs only)
    @Override
    public void enableAutoMode() {
        System.out.println("🔄 AutoOp Enabled for " + getName());
    }

    public void disableAutoMode() {
        System.out.println("🚫 Auto Mode Disabled for " + getName());
    }
}
