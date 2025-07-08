package ui.deviceActionMenu;

import devices.Device;
import devices.Light;
import ui.AutoOpController;

import java.util.Scanner;

public class LightActionsMenu {
    private static final Scanner scanner = new Scanner(System.in);

    public static void show(Device device) {
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
            System.out.println("3 - AutoOp");
            System.out.println("4 - Status");
            System.out.println("5 - Back");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> light.turnOn();
                case "2" -> light.turnOff();
                case "3" -> AutoOpController.display(light);
                case "4" -> light.status();
                case "5" -> {
                    System.out.println("↩️ Back to device menu.");
                    return;
                }
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }
}
