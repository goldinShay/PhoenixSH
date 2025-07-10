package ui.deviceActionMenu;

import devices.Device;
import devices.Light;
import ui.AutoOpController;

import java.util.Scanner;

public class LightActionsMenu {
    public static void show(Device device) {
        show(device, new Scanner(System.in)); // default user-facing behavior
    }

    public static void show(Device device, Scanner scanner) {
        if (!(device instanceof Light light)) {
            System.out.println("⚠️ This menu is only for basic Light devices.");
            return;
        }

        while (true) {
            System.out.println("\n=== Light Actions ===");
            System.out.println("Power: " + (light.isOn() ? "ON" : "OFF"));
            System.out.println("Automation: " + (light.isAutomationEnabled() ? "ENABLED" : "DISABLED"));
            System.out.println("1 - Turn ON");
            System.out.println("2 - Turn OFF");
            System.out.println("3 - AutoOp Settings");
            System.out.println("4 - Back");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> light.turnOn();
                case "2" -> light.turnOff();
                case "3" -> AutoOpController.display(light, scanner);
                case "4" -> {
                    System.out.println("↩️ Back to device menu.");
                    return;
                }
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }
}
