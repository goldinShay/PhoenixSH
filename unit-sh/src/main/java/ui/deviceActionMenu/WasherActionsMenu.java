package ui.deviceActionMenu;

import devices.Device;
import devices.WashingMachine;
import devices.actions.WashingMachineAction;
import ui.AutoOpController;

import java.util.Scanner;

public class WasherActionsMenu {
    private static final Scanner scanner = new Scanner(System.in);

    public static void show(Device device) {
        if (!(device instanceof WashingMachine washer)) {
            System.out.println("⚠️ This menu is only for Washing Machines.");
            return;
        }

        boolean isBoschFlagship = "BWM14025".equalsIgnoreCase(washer.getModel());

        while (true) {
            System.out.println("\n=== Washing Machine Actions ===");
            System.out.println("Power: " + (washer.isOn() ? "ON" : "OFF") +
                    " | Running: " + (washer.isRunning() ? "YES" : "NO"));
            System.out.println("Automation: " + (washer.isAutomationEnabled() ? "ENABLED" : "DISABLED"));

            System.out.println("1 - Turn ON");
            System.out.println("2 - Turn OFF");
            System.out.println("3 - Start Program");
            System.out.println("4 - Stop Program");
            System.out.println("5 - AutoOp");

            if (isBoschFlagship) {
                System.out.println("6 - " + WashingMachineAction.QUICK_WASH.getLabel());
                System.out.println("7 - " + WashingMachineAction.HEAVY_DUTY.getLabel());
                System.out.println("8 - " + WashingMachineAction.RINSE_AND_SPIN.getLabel());
                System.out.println("9 - Back");
            } else {
                System.out.println("6 - Advanced Programs (Not available on this model)");
                System.out.println("7 - Back");
            }

            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> washer.turnOn();
                case "2" -> washer.turnOff();
                case "3" -> washer.start();
                case "4" -> washer.stop();
                case "5" -> AutoOpController.display(washer);

                case "6" -> {
                    if (isBoschFlagship) {
                        washer.setMode(WashingMachineAction.QUICK_WASH);
                        System.out.println("🚿 " + WashingMachineAction.QUICK_WASH.getLabel() + " started.");
                    } else {
                        System.out.println("ℹ️ Advanced programs not supported on this model.");
                    }
                }

                case "7" -> {
                    if (isBoschFlagship) {
                        washer.setMode(WashingMachineAction.HEAVY_DUTY);
                        System.out.println("💪 " + WashingMachineAction.HEAVY_DUTY.getLabel() + " started.");
                    } else {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    }
                }

                case "8" -> {
                    if (isBoschFlagship) {
                        washer.setMode(WashingMachineAction.RINSE_AND_SPIN);
                        System.out.println("🔄 " + WashingMachineAction.RINSE_AND_SPIN.getLabel() + " activated.");
                    } else {
                        System.out.println("❌ Invalid option.");
                    }
                }

                case "9" -> {
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
