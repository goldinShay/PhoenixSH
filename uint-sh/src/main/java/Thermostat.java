import java.util.Random;

public class Thermostat extends Device {
    private double temperature;
    private double threshold;
    private NotificationService notificationService;
    private Random random;

    public Thermostat(String deviceId, String name, double initialTemp, double threshold, NotificationService notificationService) {
        super(deviceId, name);
        this.temperature = initialTemp;
        this.threshold = threshold;
        this.notificationService = notificationService;
        this.random = new Random();
    }

    public double getTemperature() {
        return temperature;
    }
    
    @Override
    public void simulate() {
        // TODO: Provide simulation logic that changes temperature.
        
    }
}
