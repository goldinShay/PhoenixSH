package devices;

public enum DeviceType {
    LIGHT,
    THERMOSTAT,
    WASHING_MACHINE,
    DRYER;

    @Override
    public String toString() {
        return name().replace('_', ' ').toLowerCase();
    }
}
