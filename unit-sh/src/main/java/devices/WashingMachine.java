package devices;

import storage.DeviceStorage;

import java.time.Clock;
import java.util.List;

public class WashingMachine extends Device {

    private String brand;
    private String model;
    private boolean running;
    private static final String DEFAULT_BRAND = "Unknown";
    private static final String DEFAULT_MODEL = "Unknown";
    private static int counter = 1;
    // ✅ Constructor for loading from storage
    public WashingMachine(String id, String name, String brand, String model, Clock clock) {
        super(id, name, DeviceType.WASHING_MACHINE, clock);
        this.brand = brand;
        this.model = model;
        this.running = false;
    }

    // 🌟 Start the washing machine
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
            DeviceStorage.updateDeviceState(getId(), DeviceAction.START.name());
        }
    }

    // 🌟 Stop the washing machine
    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 Washing machine stopped.");
            DeviceStorage.updateDeviceState(getId(), DeviceAction.STOP.name());
        } else {
            System.out.println("ℹ️ Washing machine is not running.");
        }
    }

    // 🔎 Getters
    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public boolean isRunning() {
        return running;
    }

    // 🌟 Provide available actions
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

    // 🌟 Perform actions based on input
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
                default -> System.out.println("❓ Unknown action for WashingMachine: " + action);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid action: " + action);
        }
    }

    // 🌟 Display washing machine status
    @Override
    public void status() {
        System.out.println("📊 WashingMachine " + getName() +
                " is " + (isOn() ? "On" : "Off") +
                ", running: " + (running ? "Yes" : "No") +
                ", brand: " + brand +
                ", model: " + model);
    }

    // 🌟 Serialize device to string format
    @Override
    public String toDataString() {
        return String.join("|", getType().name(), getId(), getName(), brand, model, String.valueOf(running));
    }

    @Override
    public String toString() {
        return "WashingMachine {" +
                "name='" + getName() + '\'' +
                ", id='" + getId() + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", power=" + (isOn() ? "On" : "Off") +
                ", running=" + (running ? "Yes" : "No") +
                '}';
    }
}
