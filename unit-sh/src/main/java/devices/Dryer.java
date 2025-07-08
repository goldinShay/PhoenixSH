package devices;

import devices.actions.DeviceAction;
import devices.actions.DryerAction;
import storage.DeviceStorage;
import utils.DeviceDefaults;

import java.time.Clock;
import java.util.List;

public class Dryer extends Device {

    // ─── Identity ───
    private final String brand;
    private final String model;
    private boolean running = false;

    private static final String DEFAULT_BRAND = "Unknown";
    private static final String DEFAULT_MODEL = "Unknown";

    // ─── Constructors ───

    public Dryer(String id, String name, String brand, String model, Clock clock) {
        super(id, name, DeviceType.DRYER, clock,
                DeviceDefaults.getDefaultAutoOn(DeviceType.DRYER),
                DeviceDefaults.getDefaultAutoOn(DeviceType.DRYER)); // Mirror OFF
        this.brand = brand != null ? brand : DEFAULT_BRAND;
        this.model = model != null ? model : DEFAULT_MODEL;
    }
    public Dryer(String id, String name, Clock clock, boolean state, double autoOn, double autoOff) {
        super(id, name, DeviceType.DRYER, clock, autoOn, autoOff);
        this.brand = "Unknown";
        this.model = "Unknown";
    }


    // ─── Runtime State ───

    public boolean isRunning() {
        return running;
    }

    // ─── Actions ───

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

    public void stop() {
        if (running) {
            running = false;
            System.out.println("🛑 Dryer stopped.");
            DeviceStorage.updateDeviceState(getId(), DeviceAction.STOP.name());
        } else {
            System.out.println("ℹ️ Dryer is not running.");
        }
    }

        public void status() {
        System.out.printf("📊 Dryer %s (%s)%n", getName(), getId());
        System.out.printf("   🔌 Power: %s | 🔁 Running: %s%n", isOn() ? "ON" : "OFF", running ? "YES" : "NO");
        System.out.printf("   🏷️ Brand: %s | Model: %s%n", brand, model);
    }

    @Override
    public List<String> getAvailableActions() {
        return List.of("on", "off", "start", "stop", "status");
    }

    @Override
    public void simulate(String action) {
        performAction(action); // Mirror behavior
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
            System.out.printf("❌ Invalid action: '%s'%n", action);
        }
    }

    // ─── Persistence ───

    public String toDataString() {
        return String.join("|",
                getType().name(),
                getId(),
                getName(),
                brand,
                model,
                String.valueOf(running));
    }

    public static Dryer fromDataString(String[] parts, Clock clock) {
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid Dryer data: " + String.join(", ", parts));
        }

        Dryer dryer = new Dryer(parts[1], parts[2], parts[3], parts[4], clock);
        dryer.running = Boolean.parseBoolean(parts[5]);
        return dryer;
    }

    @Override
    public String toString() {
        return String.format("Dryer{name='%s', id='%s', brand='%s', model='%s', power=%s, running=%s}",
                getName(), getId(), brand, model,
                isOn() ? "ON" : "OFF", running ? "YES" : "NO");
    }
    private DryerAction mode;

    public void setMode(String modeLabel) {
        if (!"BDR14025".equalsIgnoreCase(model)) {
            System.out.println("⚠️ Advanced modes not available for this model.");
            return;
        }

        try {
            this.mode = DryerAction.fromLabel(modeLabel);
            System.out.println("🔧 Mode set to: " + this.mode);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid mode: " + modeLabel);
        }
    }
    public DryerAction getMode() {
        return mode;
    }
    @Override
    public void turnOn() {
        super.setOn(true);
        System.out.println("🔌 Dryer " + getName() + " turned ON.");
    }

    @Override
    public void turnOff() {
        super.setOn(false);
        System.out.println("🔌 Dryer " + getName() + " turned OFF.");
        if (isRunning()) {
            stop(); // Interrupt running cycle gracefully
            System.out.println("🛑 Dry cycle interrupted due to power OFF.");
        }
        mode = null; // Optional: reset mode if powered off
    }



}
