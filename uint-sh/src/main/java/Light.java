public class Light extends Device {

    private boolean isOn;
    private static int counter = 1;

    // ID generator (L001, L002, ...)
    private static String generateId() {
        return "L" + String.format("%03d", counter++);
    }

    // Constructor with auto-generated ID
    public Light(String name) {
        super(generateId(), name, "light");
        this.isOn = false;
    }

    // Constructor with provided ID (used during deserialization)
    public Light(String id, String name) {
        super(id, name, "light");
        this.isOn = false;
    }

    public void turnOn() {
        isOn = true;
    }

    public void turnOff() {
        isOn = false;
    }

    public boolean getStatus() {
        return isOn;
    }

    // ✅ NEW: Set name
    public void setName(String name) {
        this.name = name;
    }

    // ✅ NEW: Set status
    public void setStatus(boolean isOn) {
        this.isOn = isOn;
    }

    @Override
    public String toDataString() {
        return "Light|" + getId() + "|" + getName();
    }

    public static Light fromDataString(String[] parts) {
        if (parts == null || parts.length < 3) {
            throw new IllegalArgumentException("Invalid data string: not enough parts to create a Light.");
        }

        String id = parts[1];
        String name = parts[2];
        return new Light(id, name);
    }

    @Override
    public void simulate() {
        // You could add a flicker simulation or toggle on/off here later
    }
}
