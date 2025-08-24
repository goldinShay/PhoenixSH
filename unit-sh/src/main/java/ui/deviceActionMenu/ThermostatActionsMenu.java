package ui.deviceActionMenu;

import devices.Device;
import devices.Thermostat;
import autoOp.AutoOpController;
import ui.gui.managers.GuiStateManager;

import java.util.Scanner;

public class ThermostatActionsMenu {

    public static void show(Device device) {
        if (!(device instanceof Thermostat thermostat)) {
            System.out.println("⚠️ This menu is only for Thermostats.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Thermostat Actions ===");
            System.out.println("Current State: " + (thermostat.isOn() ? "ON 🔥" : "OFF ❄️"));
            System.out.println("Current User Temp: " + thermostat.getUserTemp() + "°C");
            System.out.println("1 - Turn ON");
            System.out.println("2 - Turn OFF");
            System.out.println("3 - Set Default Temp (25°C)");
            System.out.println("4 - Increase Temp (+1°C)");
            System.out.println("5 - Decrease Temp (-1°C)");
            System.out.println("6 - Enable AutoOp");
            System.out.println("7 - Back");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    thermostat.turnOn();
                    GuiStateManager.refreshDeviceMatrix();
                    System.out.println("✅ Thermostat turned ON.");
                }
                case "2" -> {
                    thermostat.turnOff();
                    GuiStateManager.refreshDeviceMatrix();
                    System.out.println("🛑 Thermostat turned OFF.");
                }
                case "3" -> {
                    thermostat.setUserTemp(25.0);
                    GuiStateManager.refreshDeviceMatrix();
                    System.out.println("🌡️ User temp reset to 25°C.");
                }
                case "4" -> {
                    thermostat.increaseUserTemp();
                    GuiStateManager.refreshDeviceMatrix();
                    System.out.println("🌡️ Increased to " + thermostat.getUserTemp() + "°C.");
                }
                case "5" -> {
                    thermostat.decreaseUserTemp();
                    GuiStateManager.refreshDeviceMatrix();
                    System.out.println("🌡️ Decreased to " + thermostat.getUserTemp() + "°C.");
                }
                case "6" -> {
                    System.out.println("🔗 Launching AutoOp setup for thermostat...");
                    AutoOpController.display(thermostat, scanner);
                }
                case "7" -> {
                    System.out.println("↩️ Back to device control menu.");
                    return;
                }
                default -> System.out.println("❌ Invalid option. Please choose 1–7.");
            }
        }
    }
}
