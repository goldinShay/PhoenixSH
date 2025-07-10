package devices;

import devices.actions.DeviceAction;
import utils.NotificationService;

import java.time.Clock;
import java.util.List;

public class Thermostat extends Device {

    // ─── ⚙ Configuration & Runtime State ───
    private static final double DEFAULT_USER_TEMP = 25.0;
    private final NotificationService notificationService;
    private double userTemp;
    private double crntDevTmp = 20.20;

    // ─── 🧱 Construction ───
    public Thermostat(String id, String name, double userTemp,
                      NotificationService notificationService, Clock clock) {
        super(id, name, DeviceType.THERMOSTAT, clock,
                DeviceDefaults.getDefaultAutoOn(DeviceType.THERMOSTAT),
                DeviceDefaults.getDefaultAutoOn(DeviceType.THERMOSTAT)); // mirror OFF
        this.userTemp = userTemp;
        this.notificationService = notificationService;
    }
    public Thermostat(String id, String name, Clock clock, boolean state, double autoOn, double autoOff) {
        super(id, name, DeviceType.THERMOSTAT, clock, autoOn, autoOff);
        this.notificationService = null;
        this.userTemp = DEFAULT_USER_TEMP;
    }



    // ─── 🎛️ User Controls ───
    public void setUserTemp(double temp) {
        this.userTemp = temp;
        System.out.printf("🌡️ User Temp set to %.1f°C%n", temp);
        checkThreshold();
    }

    public void increaseUserTemp() {
        userTemp++;
        System.out.printf("🌡️ User temp increased to %.1f°C%n", userTemp);
        checkThreshold();
    }

    public void decreaseUserTemp() {
        userTemp--;
        System.out.printf("🌡️ User temp decreased to %.1f°C%n", userTemp);
        checkThreshold();
    }

    // ─── 🧠 Threshold Logic ───
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

    // ─── 📊 Status Output ───
        public void status() {
        System.out.printf("📊 Thermostat %s (%s)%n", getName(), getId());
        System.out.printf("   🔌 Power: %s%n", isOn() ? "ON" : "OFF");
        System.out.printf("   🌡️ Target: %.1f°C | Current: %.1f°C%n", userTemp, crntDevTmp);
        System.out.printf("   📏 Range: %.1f°C – %.1f°C%n", getMinThreshold(), getMaxThreshold());
    }

    // ─── 🎮 Action Handling ───
    @Override
    public List<String> getAvailableActions() {
        return List.of("on", "off", "temp_up", "temp_down", "status");
    }

    @Override
    public void simulate(String action) {
        performAction(action); // pass-through
    }

    @Override
    public void performAction(String action) {
        try {
            DeviceAction deviceAction = DeviceAction.fromString(action);
            switch (deviceAction) {
                case ON -> turnOn();
                case OFF -> turnOff();
                case TEMP_UP -> increaseUserTemp();
                case TEMP_DOWN -> decreaseUserTemp();
                case STATUS -> status();
                default -> System.out.printf("❓ Unknown action: '%s'%n", action);
            }
        } catch (IllegalArgumentException e) {
            System.out.printf("❌ Invalid action: '%s'%n", action);
        }
    }

    // ─── 📦 Persistence ───
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
        return String.format("Thermostat{name='%s', id='%s', userTemp=%.1f°C, power=%s}",
                getName(), getId(), userTemp, isOn() ? "ON" : "OFF");
    }

    // ─── Optional: Expose userTemp for other systems ───
    public double getUserTemp() {
        return userTemp;
    }
}
