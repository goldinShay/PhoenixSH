package devices;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class Dryer extends Device {
    private static int counter = 1;
    private String brand;
    private String model;
    private boolean running;

    // Constructor for loading from saved data
    public Dryer(String id, String name, String brand, String model) {
        super(id, name, "devices.devcs.Dryer", Clock.systemDefaultZone());
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // Constructor for interactive creation
    public Dryer(String name, String brand, String model, Clock clock) {
        super(generateId(), name, "devices.devcs.Dryer", clock);
        this.brand = brand;
        this.model = model;
        this.running = false;
    }
    public Dryer(String id, String name, Clock clock) {
        super(id, name, "devices.devcs.Dryer", clock);
        this.brand = "Unknown";
        this.model = "Unknown";
        this.running = false;
    }


    private static String generateId() {
        return "Dr" + String.format("%03d", counter++);
    }

    public void start() {
        if (!isOn()) {
            System.out.println("⚠️ Please turn on the dryer first.");
            return;
        }
        if (running) {
            System.out.println("🔥 devices.devcs.Dryer is already running.");
        } else {
            running = true;
            System.out.println("🧦 devices.devcs.Dryer started.");
        }
    }

    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 devices.devcs.Dryer stopped.");
        } else {
            System.out.println("ℹ️ devices.devcs.Dryer is not running.");
        }
    }
    @Override
    public List<String> getAvailableActions() {
        return List.of("start", "stop", "status");
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

    public static Dryer fromDataString(String[] parts, Clock clock) {
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid devices.devcs.Dryer data: " + Arrays.toString(parts));
        }
        String type = parts[0]; // Should be "devices.devcs.Dryer"
        String id = parts[1];
        String name = parts[2];
        String brand = parts[3];
        String model = parts[4];

        return new Dryer(id, name, brand, model);
    }


    @Override
    public void simulate() {
        // Optionally simulate drying steps
    }
    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "start":
                System.out.println("🚿 devices.devcs.Dryer " + getName() + " is now running.");
                running = true;
                break;
            case "stop":
                System.out.println("⏹️ devices.devcs.Dryer " + getName() + " has stopped.");
                running = false;
                break;
            case "status":
                System.out.println("📊 devices.devcs.Dryer " + getName() + " status: " + (running ? "Running" : "Idle"));
                break;
            default:
                System.out.println("❓ Unknown action for devices.devcs.Dryer: " + action);
        }
    }


    @Override
    public String toString() {
        return "devices.devcs.Dryer {" +
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

    @Override
    public String toDataString() {
        // Save format: type,id,name,brand,model
        return String.join("|", getType(), getId(), getName(), brand, model);
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
