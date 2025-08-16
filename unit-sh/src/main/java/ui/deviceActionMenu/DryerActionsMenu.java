package ui.deviceActionMenu;

import devices.Device;
import devices.Dryer;
import autoOp.AutoOpController;

import java.util.Scanner;

public class DryerActionsMenu {

    public static void show(Device device) {
        show(device, new Scanner(System.in)); // ✅ fallback for regular usage
    }

    public static void show(Device device, Scanner input) {
        if (!(device instanceof Dryer dryer)) {
            System.out.println("⚠️ This menu is only for Dryer devices.");
            return;
        }

        boolean isBoschFlagship = "BDR14025".equalsIgnoreCase(dryer.getModel());

        while (true) {
            System.out.println("\n=== Dryer Actions ===");
            System.out.println("Power: " + (dryer.isOn() ? "ON" : "OFF") +
                    " | Running: " + (dryer.isRunning() ? "YES" : "NO"));
            System.out.println("Automation: " + (dryer.isAutomationEnabled() ? "ENABLED" : "DISABLED"));

            System.out.println("1 - Turn ON");
            System.out.println("2 - Turn OFF");
            System.out.println("3 - Start");
            System.out.println("4 - Stop");
            System.out.println("5 - AutoOp");

            if (isBoschFlagship) {
                System.out.println("6 - EcoDry Mode");
                System.out.println("7 - RapidDry Mode");
                System.out.println("8 - AntiCrease Finish");
                System.out.println("9 - Status");
                System.out.println("10 - Back");
            } else {
                System.out.println("6 - Advanced Programs (Not available yet for this model)");
                System.out.println("7 - Status");
                System.out.println("8 - Back");
            }

            System.out.print("Choose an option: ");
            if (!input.hasNextLine()) {
                System.out.println("❌ No more input. Exiting menu.");
                return;
            }
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1" -> dryer.turnOn();
                case "2" -> dryer.turnOff();
                case "3" -> dryer.start();
                case "4" -> dryer.stop();
                case "5" -> AutoOpController.display(dryer);
                case "6" -> {
                    if (isBoschFlagship) {
                        dryer.setMode("EcoDry");
                        System.out.println("♻️ EcoDry mode activated.");
                    } else {
                        System.out.println("ℹ️ Advanced programs not available for this model.");
                    }
                }
                case "7" -> {
                    if (isBoschFlagship) {
                        dryer.setMode("RapidDry");
                        System.out.println("⚡ RapidDry mode activated.");
                    } else {
                        dryer.status();
                    }
                }
                case "8" -> {
                    if (isBoschFlagship) {
                        dryer.setMode("AntiCrease");
                        System.out.println("👔 AntiCrease mode engaged.");
                    } else {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    }
                }
                case "9" -> {
                    if (isBoschFlagship) {
                        dryer.status();
                    } else {
                        System.out.println("❌ Invalid option.");
                    }
                }
                case "10" -> {
                    if (isBoschFlagship) {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    } else {
                        System.out.println("❌ Invalid option.");
                    }
                }
                default -> System.out.println("❌ Invalid option. Please try again.");
            }
        }
    }
}
