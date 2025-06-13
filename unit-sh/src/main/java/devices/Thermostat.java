package devices;

import utils.NotificationService;

import java.time.Clock;
import java.util.List;

public class Thermostat extends Device {

    private static final double DEFAULT_USER_TEMP = 25.0;
    private static int counter = 1;

    private double userTemp;
    private final NotificationService notificationService;
    private double crntDevTmp = 20.20; // ✅ Device's actual temperature


    private static String generateId() {
        return "TM" + String.format("%03d", counter++);
    }

    public Thermostat(NotificationService notificationService, Clock clock) {
        super(generateId(), "Thermostat-" + counter, DeviceType.THERMOSTAT, clock);
        this.userTemp = DEFAULT_USER_TEMP;
        this.notificationService = notificationService;
    }

    public Thermostat(String id, String name, double userTemp,
                      NotificationService notificationService, Clock clock) {
        super(id, name, DeviceType.THERMOSTAT, clock);
        this.userTemp = userTemp;
        this.notificationService = notificationService;
    }
    public void setUserTemp(double temp) {
        this.userTemp = temp;
        System.out.println("🌡️ User Temp set to " + temp + "°C.");
        checkThreshold(); // ✅ Ensure threshold checks happen after updates
    }


    public double getUserTemp() {
        return userTemp;
    }

    public void increaseUserTemp() {
        userTemp++;
        System.out.println("🌡️ User temp increased to " + userTemp + "°C");
        checkThreshold();
    }

    public void decreaseUserTemp() {
        userTemp--;
        System.out.println("🌡️ User temp decreased to " + userTemp + "°C");
        checkThreshold();
    }

    private double getMinThreshold() {
        return userTemp - 2;
    }

    private double getMaxThreshold() {
        return userTemp + 2;
    }

    private void checkThreshold() {
        if (notificationService != null && isOn() && userTemp < getMinThreshold()) {
            notificationService.notify(getId(), "⚠️ User temp below minimum threshold!");
        }
    }

    public void userStatus() {
        System.out.println("📊 User Settings for " + getName() +
                " → Power: " + (isOn() ? "On" : "Off") +
                ", Desired Temp: " + userTemp + "°C " +
                "(Min: " + getMinThreshold() + "°C, Max: " + getMaxThreshold() + "°C)");
    }
    public void deviceStatus() {
        System.out.println("📊 Current Device Temp for " + getName() +
                " → Power: " + (isOn() ? "On" : "Off") +
                ", Current Temp: " + crntDevTmp + "°C");
    }



    @Override
    public List<String> getAvailableActions() {
        return List.of(
                DeviceAction.ON.name(),
                DeviceAction.OFF.name(),
                DeviceAction.TEMP_UP.name(),
                DeviceAction.TEMP_DOWN.name(),
                DeviceAction.STATUS.name()
        );
    }


    @Override
    public void simulate(String action) {

    }

    @Override
    public void performAction(String action) {
        try {
            DeviceAction deviceAction = DeviceAction.fromString(action); // Convert string to enum
            switch (deviceAction) {
                case ON -> turnOn();
                case OFF -> turnOff();
                case TEMP_UP -> increaseUserTemp();
                case TEMP_DOWN -> decreaseUserTemp();
                case STATUS -> userStatus(); // ✅ Updated reference
                default -> System.out.println("❓ Unknown action for Thermostat: " + action);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid action: " + action);
        }
    }


    @Override
    public String toDataString() {
        return String.join("|", getType().name(), getId(), getName(), String.valueOf(userTemp));
    }

    public static Thermostat fromDataString(String[] parts, NotificationService ns, Clock clock) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid Thermostat data: " + String.join("|", parts));
        }

        String id = parts[1];
        String name = parts[2];
        double userTemp = Double.parseDouble(parts[3]);

        return new Thermostat(id, name, userTemp, ns, clock);
    }

    @Override
    public String toString() {
        return "Thermostat {" +
                "name='" + getName() + '\'' +
                ", id='" + getId() + '\'' +
                ", userTemp=" + userTemp +
                ", minThreshold=" + getMinThreshold() +
                ", maxThreshold=" + getMaxThreshold() +
                ", power=" + (isOn() ? "On" : "Off") +
                '}';
    }
}
