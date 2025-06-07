package devices;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Light extends Device {

    private static int lightCounter = 1;
    private boolean isOn;

    // ID generator (L001, L002, ...)
    private static String generateId() {
        return "L" + String.format("%03d", lightCounter++);
    }
    @Override
    public String toDataString() {
        return "devices.Light|" + getId() + "|" + getName();
    }

    // Constructor with name + clock (auto-generates ID)
    public Light(String name, Clock clock) {
        super(generateId(), name, "devices..Light", clock);
        this.isOn = false;
    }

    // Optional: Constructor with explicit ID
    public Light(String id, String name, Clock clock) {
        super(id, name, "devices.Light", clock);
        this.isOn = false;
    }



    public static Light fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 3) {
            throw new IllegalArgumentException("Invalid data string: not enough parts to create a devices.Light." + Arrays.toString(parts));
        }

        String id = parts[1];
        String name = parts[2];
        return new Light(id, name, clock);  // ✅ Now clock is passed in
    }
    public static void initializeLightCounter(Map<String, Device> devices) {
        int max = 0;
        for (Device d : devices.values()) {
            if (d instanceof Light) {
                String id = d.getId(); // e.g., "L002"
                try {
                    int num = Integer.parseInt(id.substring(1)); // "002" -> 2
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        lightCounter = max + 1;
    }

    @Override
    public List<String> getAvailableActions() {
        return List.of("on", "off", "status");
    }



    @Override
    public void simulate() {
        // Optional: add simulation logic
    }

    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "on":
                turnOn();
                System.out.println("💡 devices.Light " + getName() + " turned ON.");
                break;
            case "off":
                turnOff();
                System.out.println("💡 devices.Light " + getName() + " turned OFF.");
                break;
            case "status":
                System.out.println("📊 devices.Light " + getName() + " status: " + (isOn() ? "ON" : "OFF"));
                break;
            default:
                System.out.println("❓ Unknown action for devices.Light: " + action);
        }
    }

}
