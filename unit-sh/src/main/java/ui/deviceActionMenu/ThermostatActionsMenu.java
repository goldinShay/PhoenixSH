package ui.deviceActionMenu;

import devices.Device;
import devices.Thermostat;

import java.util.Scanner;

public class ThermostatActionsMenu {

    private static final Scanner scanner = new Scanner(System.in);

    public static void show(Device device) {
        if (!(device instanceof Thermostat thermostat)) {
            System.out.println("⚠️ This menu is only for Thermostats.");
            return;
        }

        while (true) {
            System.out.println("\n=== Thermostat Actions ===");
            System.out.println("Current User Temp: " + thermostat.getUserTemp() + "°C");
            System.out.println("1 - Set Default Temp (25°C)");
            System.out.println("2 - Increase Temp (+1°C)");
            System.out.println("3 - Decrease Temp (-1°C)");
            System.out.println("4 - Back");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    thermostat.setUserTemp(25.0);
                    System.out.println("🌡️ User temp reset to 25°C.");
                }
                case "2" -> {
                    thermostat.increaseUserTemp();
                    System.out.println("🌡️ Increased to " + thermostat.getUserTemp() + "°C.");
                }
                case "3" -> {
                    thermostat.decreaseUserTemp();
                    System.out.println("🌡️ Decreased to " + thermostat.getUserTemp() + "°C.");
                }
                case "4" -> {
                    System.out.println("↩️ Back to device control menu.");
                    return;
                }
                default -> System.out.println("❌ Invalid option. Please choose 1-4.");
            }
        }
    }
}
