package devices;

import utils.DeviceDefaults;

import java.time.Clock;
import java.util.List;

public class GenericDevice extends Device {

    // ─── Constructor ───
    public GenericDevice(String id, String name, DeviceType type, Clock clock) {
        super(id, name, type, clock,
                DeviceDefaults.getDefaultAutoOn(type),
                DeviceDefaults.getDefaultAutoOn(type)); // Mirror OFF
    }

    // ─── Available actions ───
    @Override
    public List<String> getAvailableActions() {
        return List.of("on", "off", "status");
    }

    // ─── Simulate an action ───
    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "on" -> {
                if (!isOn()) {
                    turnOn();
                    System.out.println("🔌 Generic device " + getName() + " turned ON.");
                } else {
                    System.out.println("⚠️ " + getName() + " is already ON.");
                }
            }
            case "off" -> {
                if (isOn()) {
                    turnOff();
                    System.out.println("🛑 Generic device " + getName() + " turned OFF.");
                } else {
                    System.out.println("⚠️ " + getName() + " is already OFF.");
                }
            }
            case "status" -> status();
            default -> System.out.println("❓ Unknown action for generic device: " + action);
        }
    }

    @Override
    public void simulate() {
        System.out.println("⚙️ Simulating generic device: " + getName());
    }
    public void status() {
        System.out.printf("📊 GenericDevice %s (%s) | Power: %s%n",
                getName(), getId(), isOn() ? "ON" : "OFF");
    }

    @Override
    public String toString() {
        return String.format("GenericDevice{name='%s', id='%s', type='%s', power=%s}",
                getName(), getId(), getType(), isOn() ? "ON" : "OFF");
    }
}
