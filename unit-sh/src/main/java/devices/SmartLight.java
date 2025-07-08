package devices;

import devices.actions.SmartLightEffect;
import devices.actions.SmartLightAction;
import utils.DeviceDefaults;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartLight extends Device {

    private String brand;
    private String model;
    private SmartLightAction lightMode;
    private int red = 100;
    private int green = 100;
    private int blue = 100;
//    private Thread animationThread;
//    private volatile boolean animationRunning;
    private SmartLightEffect currentEffect = SmartLightEffect.NONE;

    public void applyColor(int r, int g, int b) {
        this.red = r;
        this.green = g;
        this.blue = b;
        // Future: push to hardware bridge or UI
        System.out.printf("🌈 %s color set to RGB(%d, %d, %d)%n", getName(), r, g, b);
    }

    // 🏗️ Constructor: full parameter set
    public SmartLight(String deviceId, String name, String brand, String model, Clock clock, boolean isOn,
                      double autoOnThreshold, double autoOffThreshold) {
        super(deviceId, name, DeviceType.SMART_LIGHT, clock, autoOnThreshold, autoOffThreshold);
        this.brand = brand != null ? brand : "Unknown";
        this.model = model != null ? model : "Unknown";
        setOn(isOn);

        if (supportsCustomMode()) {
            this.lightMode = new SmartLightAction(100, 100, 90, 80); // 📍 Default RGB mode
        }
    }

    // 🏗️ Constructor: with default thresholds
    public SmartLight(String deviceId, String name, String brand, String model, Clock clock, boolean isOn) {
        this(deviceId, name, brand, model, clock, isOn,
                DeviceDefaults.getDefaultAutoOn(DeviceType.SMART_LIGHT),
                DeviceDefaults.getDefaultAutoOn(DeviceType.SMART_LIGHT)); // Mirror OFF
    }
    public SmartLight(String deviceId, String name, Clock clock, boolean isOn,
                      double autoOnThreshold, double autoOffThreshold) {
        super(deviceId, name, DeviceType.SMART_LIGHT, clock, autoOnThreshold, autoOffThreshold);
        setOn(isOn);

        // Optional: set a default light mode
        if (supportsCustomMode()) {
            this.lightMode = new SmartLightAction(100, 100, 90, 80); // WARM_WHITE
        }
    }


    // 📝 Lightweight serialization
    public String toDataString() {
        return String.join("|", getType().name(), getId(), getName(), brand, model);
    }

    public static SmartLight fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 5) {
            throw new IllegalArgumentException("Invalid data string for SmartLight: " + String.join(", ", parts));
        }
        return new SmartLight(parts[1], parts[2], parts[3], parts[4], clock, false);
    }

    // 🟢 Capabilities
    public boolean supportsCustomMode() {
        return "Calex A60E27".equalsIgnoreCase(model);
    }

    public void setLightMode(SmartLightAction mode) {
        if (!supportsCustomMode()) {
            System.out.println("⚠️ This model does not support custom RGB light modes.");
            return;
        }
        this.lightMode = mode;
        System.out.println("🎨 Light mode set to: " + mode);
    }

    public SmartLightAction getLightMode() {
        return lightMode;
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }
    public SmartLightEffect getEffect() {
        return currentEffect;
    }


    // 🔁 Device behavior
    @Override
    public void turnOn() {
        super.setOn(true);
        System.out.println("💡 SmartLight " + getName() + " turned ON.");
    }

    @Override
    public void turnOff() {
        super.setOn(false);
        System.out.println("💡 SmartLight " + getName() + " turned OFF.");
    }

    @Override
    public List<String> getAvailableActions() {
        return supportsCustomMode()
                ? List.of("on", "off", "setMode", "status")
                : List.of("on", "off", "status");
    }

    @Override
    public void simulate(String action) {
        performAction(action);
    }

    @Override
    public void performAction(String action) {
        switch (action.toLowerCase()) {
            case "on" -> turnOn();
            case "off" -> turnOff();
            case "status" -> System.out.println(this);
            default -> System.out.println("❌ Unknown SmartLight action: " + action);
        }
    }

    @Override
    public String toString() {
        return String.format("SmartLight{name='%s', model='%s', power=%s, mode=%s}",
                getName(), model, isOn() ? "ON" : "OFF",
                lightMode != null ? lightMode : "Default");
    }
    public static Map<String, SmartLightAction> loadStaticModesFromExcel() {
        // 🚧 TODO: Load from Excel
        return new HashMap<>();
    }
    public void applyEffect(SmartLightEffect effect) {
        if (!supportsCustomMode()) {
            System.out.println("⚠️ This model does not support animated effects.");
            return;
        }

        this.currentEffect = effect != null ? effect : SmartLightEffect.NONE;
        System.out.println("🌠 Animation set to: " + this.currentEffect.name());
    }

}
