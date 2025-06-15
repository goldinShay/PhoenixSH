package devices;

import storage.DeviceStorage;
import utils.NotificationService;

import java.time.Clock;
import java.util.List;

public class Dryer extends Device {
    private String brand;
    private String model;
    private boolean running;
    private static final String DEFAULT_BRAND = "Unknown";
    private static final String DEFAULT_MODEL = "Unknown";

    // ✅ Constructor for interactive creation
    public Dryer(String id, String name, String brand, String model, Clock clock) {
        super(id, name, DeviceType.DRYER, clock);
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // ✅ Constructor for loading from file (ensures proper restoration)
    public Dryer(String id, String name, Clock clock) {
        this(id, name, DEFAULT_BRAND, DEFAULT_MODEL, clock);
    }

    // 🌟 Start the dryer
    public void start() {
        if (!isOn()) {
            System.out.println("⚠️ Dryer is OFF. Turn it on first.");
            return;
        }
        if (running) {
            System.out.println("🔥 Dryer is already running.");
        } else {
            running = true;
            System.out.println("🧦 Dryer started.");
            DeviceStorage.updateDeviceState(getId(), DeviceAction.START.name());
        }
    }

    // 🌟 Stop the dryer
    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 Dryer stopped.");
            DeviceStorage.updateDeviceState(getId(), DeviceAction.STOP.name());
        } else {
            System.out.println("ℹ️ Dryer is not running.");
        }
    }

    // 🌟 Status reporting
    @Override
    public void status() {
        System.out.println("📊 Dryer " + getName() +
                " is " + (isOn() ? "On" : "Off") +
                ", running: " + (running ? "Yes" : "No") +
                ", brand: " + brand +
                ", model: " + model);
    }

    // 🔄 Perform actions dynamically
    @Override
    public void performAction(String action) {
        try {
            DeviceAction deviceAction = DeviceAction.fromString(action);
            switch (deviceAction) {
                case ON -> turnOn();
                case OFF -> turnOff();
                case START -> start();
                case STOP -> stop();
                case STATUS -> status();
                default -> System.out.println("❓ Unknown action for Dryer: " + action);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid action: " + action);
        }
    }

    // 🌟 Available actions for Device Monitor Menu
    @Override
    public List<String> getAvailableActions() {
        return List.of(
                DeviceAction.ON.name(),
                DeviceAction.OFF.name(),
                DeviceAction.START.name(),
                DeviceAction.STOP.name(),
                DeviceAction.STATUS.name()
        );
    }

    @Override
    public void simulate(String action) {

    }

    // 🌟 Ensure device serialization is properly formatted for storage
    @Override
    public String toDataString() {
        return String.join("|",
                getType().name(),
                getId(),
                getName(),
                brand,
                model,
                String.valueOf(running)
        );
    }

    // ✅ Restore Dryer from storage
    public static Dryer fromDataString(String[] parts, Clock clock) {
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid Dryer data: " + String.join(", ", parts));
        }

        String id = parts[1];
        String name = parts[2];
        String brand = parts[3];
        String model = parts[4];
        boolean running = Boolean.parseBoolean(parts[5]);

        Dryer dryer = new Dryer(id, name, brand, model, clock);
        dryer.running = running;
        return dryer;
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
                '}';
    }
}
