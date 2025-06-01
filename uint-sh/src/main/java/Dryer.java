import java.time.Clock;
import java.util.Arrays;

public class Dryer extends Device {
    private static int counter = 1;
    private String brand;
    private String model;
    private boolean running;

    // Constructor for loading from saved data
    public Dryer(String id, String name, String brand, String model) {
        super(id, name, "Dryer", Clock.systemDefaultZone());
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // Constructor for interactive creation
    public Dryer(String name, String brand, String model, Clock clock) {
        super(generateId(), name, "Dryer", clock);
        this.brand = brand;
        this.model = model;
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
            throw new IllegalArgumentException("Invalid Dryer data: " + Arrays.toString(parts));
        }
        String type = parts[0]; // Should be "Dryer"
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
