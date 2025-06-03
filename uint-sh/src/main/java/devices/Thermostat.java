package devices;

import utils.NotificationService;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class Thermostat extends Device {
    private double temperature;
    private double threshold;
    private final NotificationService notificationService;
    private static int counter = 1;

    private static String generateId() {
        return "T" + String.format("%03d", counter++);
    }

    // ✅ Used by the menu: auto-generates ID
    public Thermostat(String name, double temperature, double threshold, NotificationService notificationService, Clock clock) {
        super(generateId(), name, "devices.Thermostat", clock);
        this.temperature = temperature;
        this.threshold = threshold;
        this.notificationService = notificationService;
    }

    // ✅ Used by deserialization (e.g., from file)
// 🔀 Reordered to avoid clash: temperature, threshold, THEN id
    public Thermostat(double temperature, double threshold, String id, NotificationService notificationService, Clock clock) {
        super(id, "devices.Thermostat-" + id, "devices.Thermostat", clock);
        this.temperature = temperature;
        this.threshold = threshold;
        this.notificationService = notificationService;
    }

    // ✅ Possibly used internally — no change needed
    public Thermostat(String id, String name, Clock clock) {
        super(id, name, "devices.Thermostat", clock);
        this.temperature = 20.0;
        this.threshold = 22.0;
        this.notificationService = null;
    }



    public void setTemperature(double temperature) {
        this.temperature = temperature;
        checkThreshold();
    }

    public double getTemperature() {
        return temperature;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    private void checkThreshold() {
        if (notificationService != null && isOn() && temperature < threshold) {
            notificationService.notify(getId(), "🌡️ Temperature below threshold!");
        }
    }

    public void increaseTemp() {
        temperature++;
        System.out.println("🌡️ Temperature increased to " + temperature + "°C");
        checkThreshold();
    }

    public void decreaseTemp() {
        temperature--;
        System.out.println("🌡️ Temperature decreased to " + temperature + "°C");
        checkThreshold();
    }

    public void status() {
        System.out.println("📊 devices.Thermostat " + getName() +
                " is " + (isOn() ? "On" : "Off") +
                ", temperature: " + temperature + "°C, threshold: " + threshold + "°C");
    }

    @Override
    public List<String> getAvailableActions() {
        return List.of("increase", "decrease", "status");
    }

    @Override
    public void simulate(String action) {
        switch (action.toLowerCase()) {
            case "increase" -> increaseTemp();
            case "decrease" -> decreaseTemp();
            case "status" -> status();
            default -> System.out.println("❓ Unknown action for devices.Thermostat: " + action);
        }
    }

    @Override
    public void simulate() {
        // Optional simulation loop
    }

    @Override
    public String toDataString() {
        return String.join("|", getType(), getId(), getName(), String.valueOf(temperature), String.valueOf(threshold));
    }

    public static Thermostat fromDataString(String[] parts, NotificationService ns, Clock clock) {
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid devices.Thermostat data: " + Arrays.toString(parts));
        }

        String id = parts[1];
        String name = parts[2];
        double temperature = Double.parseDouble(parts[3]);
        double threshold = Double.parseDouble(parts[4]);

        Thermostat t = new Thermostat(temperature, threshold, id, ns, clock);
        t.setName(name); // Preserve original name
        return t;
    }


    @Override
    public String toString() {
        return "devices.Thermostat {" +
                "name='" + getName() + '\'' +
                ", id='" + getId() + '\'' +
                ", temperature=" + temperature +
                ", threshold=" + threshold +
                ", power=" + (isOn() ? "On" : "Off") +
                '}';
    }
}
