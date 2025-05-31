// Base class for all devices
public abstract class Device implements Runnable {
    protected String deviceId;
    protected String name;
    protected String type;

    public Device(String deviceId, String name, String type) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    // Added for consistency with getName() in menus etc.
    public String getId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public abstract void simulate();

    @Override
    public void run() {
        simulate();
    }
    public abstract String toDataString(); // Abstract method

}
