package devices;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class Light extends Device {

    private static int counter = 1;
    private boolean isOn;

    // ID generator (L001, L002, ...)
    private static String generateId() {
        return "L" + String.format("%03d", counter++);
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
