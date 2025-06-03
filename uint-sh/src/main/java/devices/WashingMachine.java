package devices;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class WashingMachine extends Device {
    private String brand;
    private String model;
    private boolean running;
    private static int counter = 1;

    private static String generateId() {
        return "WM" + String.format("%03d", counter++);
    }

    // ✅ Constructor for loading from saved data (brand/model provided)
    public WashingMachine(String id, String name, String brand, String model) {
        super(id, name, "devices.devcs.WashingMachine", Clock.systemDefaultZone());
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // ✅ Constructor for interactive creation (brand/model provided, id auto-generated)
    public WashingMachine(String name, String brand, String model, Clock clock) {
        super(generateId(), name, "devices.devcs.WashingMachine", clock);
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // ✅ Constructor for devices.devcs.DeviceFactory use (brand/model unknown)
    public WashingMachine(String id, String name, Clock clock) {
        super(id, name, "devices.devcs.WashingMachine", clock);
        this.brand = "Unknown";
        this.model = "Unknown";
        this.running = false;
    }

    public void start() {
        if (!isOn()) {
            System.out.println("⚠️ Please turn on the washing machine first.");
            return;
        }
        if (running) {
            System.out.println("🌀 Washing machine is already running.");
        } else {
            running = true;
            System.out.println("🧺 Washing machine started.");
        }
    }

    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 Washing machine stopped.");
        } else {
            System.out.println("ℹ️ Washing machine is not running.");
        }
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public boolean isRunning() {
        return running;
    }

    public static WashingMachine fromDataString(String[] parts, Clock clock) {
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid devices.devcs.WashingMachine data: " + Arrays.toString(parts));
        }
        String id = parts[1];
        String name = parts[2];
        String brand = parts[3];
        String model = parts[4];
        return new WashingMachine(id, name, brand, model);
    }

    @Override
    public String toString() {
        return "devices.devcs.WashingMachine {" +
                "name='" + getName() + '\'' +
                ", id='" + getId() + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", power=" + (isOn() ? "On" : "Off") +
                ", running=" + (running ? "Yes" : "No") +
                '}';
    }

    @Override
    public List<String> getAvailableActions() {
        return List.of("start", "stop", "status");
    }

    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "start":
                start();
                break;
            case "stop":
                stop();
                break;
            case "status":
                System.out.println("📊 Washing machine " + getName()
                        + " is " + (isOn() ? "On" : "Off")
                        + " and " + (running ? "Running" : "Idle"));
                break;
            default:
                System.out.println("❓ Unknown action for devices.devcs.WashingMachine: " + action);
        }
    }

    @Override
    public void simulate() {
        // Optional default simulation logic
    }

    @Override
    public String toDataString() {
        return String.join("|", getType(), getId(), getName(), brand, model);
    }
}
