package devices;

public enum DeviceStatus {
    ON,
    OFF,
    PAUSED,
    RUNNING,
    IDLE;


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
