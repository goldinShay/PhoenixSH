package devices;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class GenericDevice extends Device {

    @Override
    public void status() {

    }

    public GenericDevice(String id, String name, DeviceType type, Clock clock) {
        super(id, name, type, clock);
    }

    @Override
    public List<String> getAvailableActions() {
        return Arrays.asList("on", "off", "status");
    }

    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "on":
                if (!isOn()) {
                    turnOn();
                    System.out.println("🔌 Generic device " + getName() + " turned ON.");
                } else {
                    System.out.println("⚠️ " + getName() + " is already ON.");
                }
                break;
            case "off":
                if (isOn()) {
                    turnOff();
                    System.out.println("🛑 Generic device " + getName() + " turned OFF.");
                } else {
                    System.out.println("⚠️ " + getName() + " is already OFF.");
                }
                break;
            case "status":
                System.out.println("📊 " + getType() + " " + getName() + " is " + (isOn() ? "ON" : "OFF"));
                break;
            default:
                System.out.println("❓ Unknown action for generic device: " + action);
        }
    }

    @Override
    public void simulate() {
        System.out.println("⚙️ Simulating generic device: " + getName());
    }

    @Override
    public String toDataString() {
        return String.join("|",
                getType().toString(),
                getId(),
                getName()
        );
    }

    @Override
    public String toString() {
        return "devices.GenericDevice {" +
                "name='" + getName() + '\'' +
                ", id='" + getId() + '\'' +
                ", type='" + getType() + '\'' +
                ", power=" + (isOn() ? "On" : "Off") +
                '}';
    }
}
