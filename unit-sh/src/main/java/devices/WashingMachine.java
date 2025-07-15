package devices;

import devices.actions.DeviceAction;
import devices.actions.WashingMachineAction;
import storage.DeviceStorage;

import java.time.Clock;
import java.util.List;

public class WashingMachine extends Device {

    // ─── 🔑 Identity ───
    private String brand;
    private String model;
    private boolean running = false;
    private String currentMode = "Standard";


    // ─── 🏗 Constructors ───

    // Full constructor (with thresholds)
    public WashingMachine(String id, String name, String brand, String model, Clock clock,
                          boolean isOn, double autoOnThreshold, double autoOffThreshold,
                          boolean skipIdCheck) {
        super(id, name, DeviceType.WASHING_MACHINE, clock, autoOnThreshold, autoOffThreshold, skipIdCheck);
        this.brand = brand != null ? brand : "Unknown";
        this.model = model != null ? model : "Unknown";
        setOn(isOn);
    }



    // ─── ⚙ State Control ───

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

    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 Washing machine stopped.");
            DeviceStorage.updateDeviceState(getId(), DeviceAction.STOP.name());
        } else {
            System.out.println("ℹ️ Washing machine is not running.");
        }
    }


    public void status() {
        System.out.printf("📊 WashingMachine %s (Brand: %s, Model: %s)%n", getName(), brand, model);
        System.out.printf("   🔌 Power: %s | 🌀 Running: %s%n", isOn() ? "ON" : "OFF", running ? "YES" : "NO");
    }

    // ─── 🚦 Action Handling ───

    @Override
    public List<String> getAvailableActions() {
        return List.of("on", "off", "start", "stop", "status");
    }

    @Override
    public void simulate(String action) {
        performAction(action);
    }

    @Override
    public void performAction(String action) {
        try {
            DeviceAction act = DeviceAction.fromString(action);
            switch (act) {
                case ON -> turnOn();
                case OFF -> turnOff();
                case START -> start();
                case STOP -> stop();
                case STATUS -> status();
                default -> System.out.printf("❓ Unknown action: '%s'%n", action);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid action: " + action);
        }
    }

    // ─── 🧺 Metadata Getters ───

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public boolean isRunning() {
        return running;
    }

    // ─── 📦 Serialization ───

    public String toDataString() {
        return String.join("|",
                getType().name(),
                getId(),
                getName(),
                brand,
                model,
                String.valueOf(running));
    }

    @Override
    public String toString() {
        return String.format("WashingMachine{name='%s', id='%s', brand='%s', model='%s', power=%s, running=%s}",
                getName(), getId(), brand, model,
                isOn() ? "ON" : "OFF", running ? "YES" : "NO");
    }
    public void setMode(WashingMachineAction mode) {
        if (mode == null) {
            System.out.println("❌ Cannot set mode: null provided.");
            return;
        }

        // Optional: prevent mode changes if machine is OFF
        if (!isOn()) {
            System.out.println("⚠️ Please turn on the washing machine before selecting a program.");
            return;
        }

        this.currentMode = mode.getLabel();
        System.out.printf("✅ Mode set to '%s' (%d°C, %d rpm)%n",
                mode.getLabel(), mode.getWaterTemp(), mode.getSpinSpeed());
    }
    @Override
    public void turnOff() {
        super.setOn(false);
        System.out.println("🔌 Washing Machine " + getName() + " turned OFF.");

        if (isRunning()) {
            stop(); // Gracefully end the running program
            System.out.println("🛑 Wash cycle interrupted due to power OFF.");
        }

        if (currentMode != null && !currentMode.equals("Standard")) {
            currentMode = "Standard"; // Resets to default
            System.out.println("📴 Program reset to 'Standard'.");
        }
    }


}
