// Base class for all devices
public abstract class Device implements Runnable {
    protected String deviceId;
    protected String name;
    protected String type;
    private boolean isOn;

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

    // ✅ Added setter so devices can be renamed during edit
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    // ✅ Optional: if you'll ever want to change type too in the future
    public void setType(String type) {
        this.type = type;
    }

    public void turnOn() {
        isOn = true;
    }

    public void turnOff() {
        isOn = false;
    }

    public boolean isOn() {
        return isOn;
    }

    public abstract void simulate();

    @Override
    public void run() {
        simulate();
    }

    public abstract String toDataString(); // Abstract method for persistence
}
