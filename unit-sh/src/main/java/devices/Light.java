package devices;

import storage.DeviceStorage;
import storage.XlCreator;

import java.time.Clock;
import java.util.*;

public class Light extends Device {

    private boolean isOn;

    // ID generator using XlCreator
    public static String generateId(Set<String> allIds) {
        System.out.println("🛠️ Debug - Existing IDs Before Generating New One: " + allIds);
        return XlCreator.getNextAvailableId("LI", allIds);
    }


    // Constructor with name + clock + all existing IDs
    public Light(String name, Clock clock) {
        super(generateNewId(), name, DeviceType.LIGHT, clock);  // ✅ Call to `super()` comes first
        this.isOn = false;
    }

    // 🌟 Static method to properly generate a unique ID
    private static String generateNewId() {
        Set<String> allIds = new HashSet<>(DeviceStorage.getDevices().keySet());  // ✅ Get stored IDs
        return XlCreator.getNextAvailableId("LI", allIds);  // ✅ Generate a unique ID
    }




    // Optional: Constructor with explicit ID
    public Light(String id, String name, Clock clock) {
        super(id, name, DeviceType.LIGHT, clock);
        this.isOn = false;
    }

    // For serialization
    @Override
    public String toDataString() {
        return getType() + "|" + getId() + "|" + getName();
    }

    public static Light fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 3) {
            throw new IllegalArgumentException("Invalid data string: not enough parts to create a Light. " + Arrays.toString(parts));
        }

        String id = parts[1];
        String name = parts[2];
        return new Light(id, name, clock);
    }

    // Not needed anymore if using generateId with existing IDs
    @Deprecated
    public static void initializeLightCounter(Map<String, Device> devices) {
        // No-op
    }

    @Override
    public List<String> getAvailableActions() {
        return List.of(
                DeviceAction.ON.name().toLowerCase(),
                DeviceAction.OFF.name().toLowerCase(),
                DeviceAction.STATUS.name().toLowerCase()
        );
    }

    @Override
    public void simulate() {
        // Optional logic
    }

    @Override
    public void simulate(String action) {
        DeviceAction act;
        try {
            act = DeviceAction.fromString(action);
        } catch (IllegalArgumentException e) {
            System.out.println("❓ Unknown action for Light: " + action);
            return;
        }

        switch (act) {
            case ON:
                turnOn();
                System.out.println("💡 Light " + getName() + " turned ON.");
                break;
            case OFF:
                turnOff();
                System.out.println("💡 Light " + getName() + " turned OFF.");
                break;
            case STATUS:
                System.out.println("📊 Light " + getName() + " status: " + (isOn() ? "ON" : "OFF"));
                break;
            default:
                System.out.println("❓ Action not supported by Light: " + action);
        }
    }

    // Helper methods
    @Override
    public void turnOn() {
        this.isOn = true;
    }

    @Override
    public void turnOff() {
        this.isOn = false;
    }

    @Override
    public boolean isOn() {
        return isOn;
    }

}
