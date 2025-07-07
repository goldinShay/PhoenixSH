package ui.deviceActionMenu;

import devices.Device;
import devices.Dryer;

import java.util.Scanner;

public class DryerActionsMenu {
    private static final Scanner scanner = new Scanner(System.in);

    public static void show(Device device) {
        if (!(device instanceof Dryer dryer)) {
            System.out.println("⚠️ This menu is only for Dryer devices.");
            return;
        }

        boolean isBoschFlagship = "BDR14025".equalsIgnoreCase(dryer.getModel());

        while (true) {
            System.out.println("\n=== Dryer Actions ===");
            System.out.println("Power: " + (dryer.isOn() ? "ON" : "OFF") +
                    " | Running: " + (dryer.isRunning() ? "YES" : "NO"));
            System.out.println("1 - Start");
            System.out.println("2 - Stop");

            if (isBoschFlagship) {
                System.out.println("3 - EcoDry Mode");
                System.out.println("4 - RapidDry Mode");
                System.out.println("5 - AntiCrease Finish");
                System.out.println("6 - Back");
            } else {
                System.out.println("3 - Advanced Programs (Not available yet for this model)");
                System.out.println("4 - Back");
            }

            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> dryer.start();
                case "2" -> dryer.stop();

                case "3" -> {
                    if (isBoschFlagship) {
                        dryer.setMode("EcoDry");
                        System.out.println("♻️ EcoDry mode activated.");
                    } else {
                        System.out.println("ℹ️ This model does not support advanced modes.");
                    }
                }
                case "4" -> {
                    if (isBoschFlagship) {
                        dryer.setMode("RapidDry");
                        System.out.println("⚡ RapidDry mode activated.");
                    } else {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    }
                }
                case "5" -> {
                    if (isBoschFlagship) {
                        dryer.setMode("AntiCrease");
                        System.out.println("👔 AntiCrease mode engaged.");
                    } else {
                        System.out.println("❌ Invalid option.");
                    }
                }
                case "6" -> {
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
