package devices.actions;

import devices.DeviceType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum ApprovedDeviceModel {

    // Plain Lights
    OSRAM_CLASSIC_A60(DeviceType.LIGHT, "Osram", "Classic A60"),
    PHILIPS_LED_BULB(DeviceType.LIGHT, "Philips", "LED Bulb"),
    SYLVANIA_SOFTWHITE(DeviceType.LIGHT, "Sylvania", "Soft White"),

    // Smart Lights
    PHILIPS_HUE(DeviceType.SMART_LIGHT, "Philips", "Hue", "RGB", "Dimmable"),
    LIFX_BEAM(DeviceType.SMART_LIGHT, "LIFX", "Beam", "RGB"),
    CALEX_A60E27(DeviceType.SMART_LIGHT, "CALEX", "A60E27", "RGB"),

    // Thermostats
    NEST_GEN3(DeviceType.THERMOSTAT, "Google", "Nest Gen3"),
    TADO_V3(DeviceType.THERMOSTAT, "Tado", "V3"),

    // Washing Machines
    SAMSUNG_BUBBLE(DeviceType.WASHING_MACHINE, "Samsung", "EcoBubble"),
    LG_TWINWASH(DeviceType.WASHING_MACHINE, "LG", "TwinWash"),

    // Dryers
    BOSCH_SERIES6(DeviceType.DRYER, "Bosch", "Series 6"),
    WHIRLPOOL_FRESHCARE(DeviceType.DRYER, "Whirlpool", "FreshCare+");

    private final DeviceType type;
    private final String brand;
    private final String model;
    private final Set<String> features;

    ApprovedDeviceModel(DeviceType type, String brand, String model, String... features) {
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.features = Set.of(features);
    }

    public String brand() { return brand; }
    public String model() { return model; }
    public DeviceType getDeviceType() { return type; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Set<String> getFeatures() { return features; }

    // 🔍 Check if model supports a feature
    public boolean hasFeature(String token) {
        return features.contains(token);
    }

    // 🔍 Find a specific approved model
    public static ApprovedDeviceModel lookup(String brand, String model) {
        if (brand == null || model == null) return null;

        return Arrays.stream(values())
                .filter(d -> d.brand.equalsIgnoreCase(brand.trim()) &&
                        d.model.equalsIgnoreCase(model.trim()))
                .findFirst()
                .orElse(null);
    }

    // 📋 Get all models matching a DeviceType
    public static List<ApprovedDeviceModel> getByType(DeviceType deviceType) {
        return Arrays.stream(values())
                .filter(d -> d.type == deviceType)
                .toList();
    }
}
