package devices;

import devices.actions.SmartLightAction;
import devices.actions.SmartLightEffect;
import devices.actions.SmartLightColorMode;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartLight extends Device {

    private String brand;
    private String model;
    private SmartLightAction lightMode;
    private SmartLightColorMode colorMode = SmartLightColorMode.WARM_WHITE;
    private int red = colorMode.getRed();
    private int green = colorMode.getGreen();
    private int blue = colorMode.getBlue();
    private SmartLightEffect currentEffect = SmartLightEffect.NONE;

    // 🏗️ Constructor: full parameter set
    public SmartLight(String deviceId, String name, String brand, String model, Clock clock,
                      boolean isOn, double autoOnThreshold, double autoOffThreshold, boolean skipIdCheck) {
        super(deviceId, name, DeviceType.SMART_LIGHT, clock, autoOnThreshold, autoOffThreshold, skipIdCheck);
        this.brand = brand != null ? brand : "Unknown";
        this.model = model != null ? model : "Unknown";
        setOn(isOn);

        setColorMode(SmartLightColorMode.WARM_WHITE);

        if (supportsCustomMode()) {
            this.lightMode = new SmartLightAction("WARM_WHITE", 100, red, green, blue);
        }
    }

    // 🏗️ With default thresholds
    public SmartLight(String deviceId, String name, String brand, String model, Clock clock, boolean isOn) {
        this(deviceId, name, brand, model, clock, isOn,
                DeviceDefaults.getDefaultAutoOn(DeviceType.SMART_LIGHT),
                DeviceDefaults.getDefaultAutoOff(DeviceType.SMART_LIGHT),
                false);
    }

    // 🛠️ Minimal constructor
    public SmartLight(String deviceId, String name, Clock clock,
                      boolean isOn, double autoOnThreshold, double autoOffThreshold, boolean skipIdCheck) {
        this(deviceId, name, null, null, clock, isOn, autoOnThreshold, autoOffThreshold, skipIdCheck);
    }

    // 🎨 Color + Mode Handling
    public void applyColor(int r, int g, int b) {
        this.red = r;
        this.green = g;
        this.blue = b;

        // 🌈 Detect preset or fallback to CUSTOM
        this.colorMode = SmartLightColorMode.CUSTOM;
        for (SmartLightColorMode mode : SmartLightColorMode.values()) {
            if (mode.getRed() == r && mode.getGreen() == g && mode.getBlue() == b && !mode.isCustom()) {
                this.colorMode = mode;
                break;
            }
        }

        System.out.printf("🌈 %s color set to RGB(%d, %d, %d)%n", getName(), r, g, b);
    }

    public void setColorMode(SmartLightColorMode mode) {
        if (mode == null) return;
        this.colorMode = mode;
        applyColor(mode.getRed(), mode.getGreen(), mode.getBlue());
    }

    public SmartLightColorMode getColorMode() {
        return colorMode;
    }

    // 💡 Mode Editor
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

    // 📝 Metadata
    public String getModel() { return model; }
    public String getBrand() { return brand; }
    public SmartLightEffect getEffect() { return currentEffect; }

    // 🔁 Device behavior
    @Override
    public void turnOn() {
        super.setOn(true);
        System.out.printf("💡 SmartLight '%s' turned ON with color mode [%s]%n",
                getName(), colorMode.getLabel());
    }

    @Override
    public void turnOff() {
        super.setOn(false);
        System.out.println("💡 SmartLight " + getName() + " turned OFF.");
    }

    @Override
    public List<String> getAvailableActions() {
        return supportsCustomMode()
                ? List.of("on", "off", "setMode", "status", "editRGB")
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
        return String.format("SmartLight{name='%s', model='%s', power=%s, colorMode=%s, effect=%s}",
                getName(), model, isOn() ? "ON" : "OFF",
                colorMode.getLabel(), currentEffect.name());
    }

    public void applyEffect(SmartLightEffect effect) {
        if (!supportsCustomMode()) {
            System.out.println("⚠️ This model does not support animated effects.");
            return;
        }

        this.currentEffect = effect != null ? effect : SmartLightEffect.NONE;
        System.out.println("🌠 Animation set to: " + this.currentEffect.name());
    }

    public boolean supportsCustomMode() {
        return "Calex A60E27".equalsIgnoreCase(model);
    }

    public static Map<String, SmartLightAction> loadStaticModesFromExcel() {
        return new HashMap<>();
    }

    public String toDataString() {
        return String.join("|", getType().name(), getId(), getName(), brand, model, colorMode.getLabel());
    }

    public static SmartLight fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 6) {
            throw new IllegalArgumentException("Invalid data string for SmartLight: " + String.join(", ", parts));
        }

        SmartLight light = new SmartLight(parts[1], parts[2], parts[3], parts[4], clock, false);
        light.setColorMode(SmartLightColorMode.fromLabel(parts[5]));
        return light;
    }
}
