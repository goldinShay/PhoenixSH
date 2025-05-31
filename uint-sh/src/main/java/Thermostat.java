import java.util.Random;

public class Thermostat extends Device {
    private double temperature;
    private double threshold;
    private NotificationService notificationService;
    private Random random;
    private static int counter = 1;

    private static String generateId() {
        return "T" + String.format("%03d", counter++);
    }

    // Full constructor with id
    public Thermostat(String id, String name, double initialTemp, double threshold, NotificationService notificationService) {
        super(id, name, "thermostat");
        this.temperature = initialTemp;
        this.threshold = threshold;
        this.notificationService = notificationService;
        this.random = new Random();
    }

    // Convenience constructor (auto-generates ID)
    public Thermostat(String name, double initialTemp, double threshold, NotificationService notificationService) {
        this(generateId(), name, initialTemp, threshold, notificationService);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public void simulate() {
        double change = (random.nextDouble() * 2) - 1; // range [-1.0, +1.0]
        temperature += change;

        if (Math.abs(temperature - threshold) > 2.0) {
            notificationService.notify("Thermostat " + getName() + " exceeded threshold! Current: " + temperature);
        }
    }

    @Override
    public String toDataString() {
        return "Thermostat|" + getId() + "|" + getName() + "|thermostat|" + temperature + "|" + threshold;
    }

    public static Thermostat fromDataString(String[] parts, NotificationService service) {
        if (parts == null || parts.length < 6) {
            throw new IllegalArgumentException("Invalid data string: not enough parts to create a Thermostat.");
        }

        try {
            String id = parts[1];
            String name = parts[2];
            String type = parts[3];

            if (!"thermostat".equalsIgnoreCase(type)) {
                throw new IllegalArgumentException("Invalid type: expected 'thermostat', got '" + type + "'");
            }

            double temperature = Double.parseDouble(parts[4]);
            double threshold = Double.parseDouble(parts[5]);

            return new Thermostat(id, name, temperature, threshold, service);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in temperature fields.", e);
        }
    }
}
