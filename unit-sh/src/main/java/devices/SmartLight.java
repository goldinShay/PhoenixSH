package devices;

import devices.actions.DeviceAction;
import devices.actions.SmartLightColorMode;
import devices.actions.SmartLightEffect;
import devices.actions.ApprovedDeviceModel;
import utils.Log;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartLight extends Device {

    private String brand;
    private String model;
    private SmartLightColorMode colorMode;
    private SmartLightEffect lightFx;

    // ───────────────────────── Constructors ─────────────────────────

    public SmartLight(String deviceId, String name, ApprovedDeviceModel approved, Clock clock,
                      boolean isOn, double autoOnThreshold, double autoOffThreshold, boolean skipIdCheck) {
        super(deviceId, name, DeviceType.SMART_LIGHT, clock, autoOnThreshold, autoOffThreshold, skipIdCheck);

        this.brand = approved != null ? approved.getBrand() : "Unknown";
        this.model = approved != null ? approved.getModel() : "Unknown";

        this.setBrand(this.brand);
        this.setModel(this.model);

        setOn(isOn);
        this.colorMode = SmartLightColorMode.WARM_WHITE;
        this.lightFx = SmartLightEffect.NONE;
    }

    public SmartLight(String deviceId, String name, ApprovedDeviceModel approved, Clock clock, boolean isOn) {
        this(deviceId, name, approved,
                clock, isOn,
                DeviceDefaults.getDefaultAutoOn(DeviceType.SMART_LIGHT),
                DeviceDefaults.getDefaultAutoOff(DeviceType.SMART_LIGHT),
                false);
    }

    public SmartLight(String deviceId, String name, Clock clock, boolean isOn,
                      double autoOnThreshold, double autoOffThreshold, boolean skipIdCheck) {
        this(deviceId, name, null, clock, isOn, autoOnThreshold, autoOffThreshold, skipIdCheck);
    }

    // ───────────────────────── Mode Setters ─────────────────────────

    public void setColorMode(SmartLightColorMode mode) {
        if (mode == null) return;
        this.colorMode = mode;
        System.out.println("🎨 Color mode set to: " + mode.getLabel());
    }

    public void setLiteFx(SmartLightEffect effect) {
        if (!supportsCustomMode()) {
            System.out.println("⚠️ Animated effects not supported by this model.");
            return;
        }
        this.lightFx = effect != null ? effect : SmartLightEffect.NONE;
        System.out.println("🌠 Effect set to: " + this.lightFx.getDescription());
    }

    // ───────────────────────── Metadata ─────────────────────────

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public SmartLightColorMode getColorMode() { return colorMode; }
    public SmartLightEffect getLiteFx() { return lightFx; }

    //Todo: why is this here and not directed to: ApprovedDeviceModel?
    public boolean supportsCustomMode() {
        return "Calex A60E27".equalsIgnoreCase(model);
    }

    public String toDataString() {
        return String.join("|", getType().name(), getId(), getName(), brand, model, colorMode.getLabel());
    }

    public static SmartLight fromDataString(String[] parts, Clock clock) {
        if (parts == null || parts.length < 6) {
            throw new IllegalArgumentException("Invalid data string for SmartLight: " + String.join(", ", parts));
        }

        ApprovedDeviceModel approved = lookup(parts[3], parts[4]);
        SmartLight light = new SmartLight(parts[1], parts[2], approved, clock, false);
        light.setColorMode(SmartLightColorMode.fromLabel(parts[5]));
        return light;
    }

    public static ApprovedDeviceModel lookup(String brand, String model) {
        for (ApprovedDeviceModel d : ApprovedDeviceModel.values()) {
            if (d.getBrand().equalsIgnoreCase(brand) && d.getModel().equalsIgnoreCase(model)) {
                return d;
            }
        }
        return null;
    }

    // ───────────────────────── Device Behavior ─────────────────────────

    @Override
    public void turnOn() {
        super.setOn(true);
        System.out.printf("💡 SmartLight '%s' turned ON with [%s] + FX: %s%n",
                getName(), colorMode.getLabel(), lightFx.name());
    }

    @Override
    public void turnOff() {
        super.setOn(false);
        System.out.println("💡 SmartLight " + getName() + " turned OFF.");
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
                colorMode.getLabel(), lightFx.name());
    }

    @Override
    public List<String> getAvailableActions() {
        return supportsCustomMode()
                ? List.of("on", "off", "setColor", "setEffect", "status")
                : List.of("on", "off", "status");
    }
    public void setSupportedActionsFromText(String actionsText) {
        if (actionsText == null || actionsText.isBlank()) {
            this.setActions(Collections.emptyList());
            return;
        }

        String[] tokens = actionsText.split(",");
        List<DeviceAction> actions = new ArrayList<>();
        for (String token : tokens) {
            try {
                actions.add(DeviceAction.valueOf(token.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                Log.warn("⚠️ Unknown action ignored: " + token);
            }
        }

        this.setActions(actions);
    }
}
