import java.time.Clock;

public class Light extends Device {

    private static int counter = 1;
    private boolean isOn;

    // ID generator (L001, L002, ...)
    private static String generateId() {
        return "L" + String.format("%03d", counter++);
    }
    @Override
    public String toDataString() {
        return "Light|" + getId() + "|" + getName();
    }

    // Constructor with name + clock (auto-generates ID)
    public Light(String name, Clock clock) {
        super(generateId(), name, "light", clock);
        this.isOn = false;
    }

    // Optional: Constructor with explicit ID
    public Light(String id, String name, Clock clock) {
        super(id, name, "light", clock);
        this.isOn = false;
    }



    public static Light fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 3) {
            throw new IllegalArgumentException("Invalid data string: not enough parts to create a Light.");
        }

        String id = parts[1];
        String name = parts[2];
        return new Light(id, name, clock);  // ✅ Now clock is passed in
    }


    @Override
    public void simulate() {
        // Optional: add simulation logic
    }
}
