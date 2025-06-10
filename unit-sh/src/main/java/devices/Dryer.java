package devices;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class Dryer extends Device {
    private static int counter = 1;
    private String brand;
    private String model;
    private boolean running;

    private static String generateId() {
        return "Dr" + String.format("%03d", counter++);
    }

    // Used for interactive creation
    public Dryer(String name, String brand, String model, Clock clock) {
        super(generateId(), name, DeviceType.DRYER, clock);
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // Used when loading from file (with known id)
    public Dryer(String id, String name, String brand, String model) {
        super(id, name, DeviceType.DRYER, Clock.systemDefaultZone());
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // Optional: minimal constructor for generic creation
    public Dryer(String id, String name, Clock clock) {
        super(id, name, DeviceType.DRYER, clock);
        this.brand = "Unknown";
        this.model = "Unknown";
        this.running = false;
    }

    public void start() {
        if (!isOn()) {
            System.out.println("⚠️ Please turn on the dryer first.");
            return;
        }
        if (running) {
            System.out.println("🔥 Dryer is already running.");
        } else {
            running = true;
            System.out.println("🧦 Dryer started.");
        }
    }

    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 Dryer stopped.");
        } else {
            System.out.println("ℹ️ Dryer is not running.");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "start" -> start();
            case "stop" -> stop();
            case "status" -> System.out.println("📊 Dryer " + getName() + " status: " + (running ? "Running" : "Idle"));
            default -> System.out.println("❓ Unknown action for Dryer: " + action);
        }
    }

    @Override
    public void simulate() {
        // Optional simulation logic
    }

    @Override
    public List<String> getAvailableActions() {
        return List.of("start", "stop", "status");
    }

    @Override
    public String toDataString() {
        return String.join("|",
                getType().toString(),  // Convert enum to String
                getId(),
                getName(),
                brand,
                model
        );
    }


    public static Dryer fromDataString(String[] parts, Clock clock) {
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid Dryer data: " + Arrays.toString(parts));
        }

        String id = parts[1];
        String name = parts[2];
        String brand = parts[3];
        String model = parts[4];

        return new Dryer(id, name, brand, model);
    }

    @Override
    public String toString() {
        return "Dryer {" +
                "name='" + getName() + '\'' +
                ", id='" + getId() + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", power=" + (isOn() ? "On" : "Off") +
                ", running=" + (running ? "Yes" : "No") +
                ", lastOn=" + getLastOnTimestamp() +
                ", lastOff=" + getLastOffTimestamp() +
                '}';
    }
}
