package ui.deviceActionMenu;

import devices.Device;
import devices.Dryer;
import devices.actions.DryerAction;
import devices.actions.actionSimulator.DryerCycleSimulator;
import devices.actions.advancedActions.DryerActionsBoschSeries6;
import autoOp.AutoOpController;

import java.util.List;
import java.util.Scanner;

public class DryerActionsMenu {

    public static void show(Device device) {
        show(device, new Scanner(System.in));
    }

    public static void show(Device device, Scanner input) {
        if (!(device instanceof Dryer dryer)) {
            System.out.println("⚠️ This menu is only for Dryer devices.");
            return;
        }

        boolean isBoschSeries6 = DryerActionsBoschSeries6.isCompatible(dryer.getBrand(), dryer.getModel());
        List<DryerAction> advancedPrograms = isBoschSeries6
                ? DryerActionsBoschSeries6.getAvailablePrograms()
                : List.of();

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

            if (isBoschSeries6) {
                int optionIndex = 6;
                for (DryerAction action : advancedPrograms) {
                    System.out.println(optionIndex++ + " - " + action.getLabel() + " Mode");
                }
                System.out.println(optionIndex++ + " - Status");
                System.out.println(optionIndex + " - Back");
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
                case "6", "7", "8" -> {
                    if (isBoschSeries6) {
                        int index = Integer.parseInt(choice) - 6;
                        if (index < advancedPrograms.size()) {
                            DryerAction selected = advancedPrograms.get(index);
                            dryer.setMode(selected.getLabel());
                            System.out.println("✅ " + selected.getLabel() + " mode activated (" +
                                    selected.getDurationMinutes() + " mins).");

                            // ⏳ Start countdown simulation
                            DryerCycleSimulator.simulateCycle(selected);

                        } else if (index == advancedPrograms.size()) {
                            dryer.status();
                        } else if (index == advancedPrograms.size() + 1) {
                            System.out.println("↩️ Back to device menu.");
                            return;
                        } else {
                            System.out.println("❌ Invalid option.");
                        }
                    } else {
                        switch (choice) {
                            case "6" -> System.out.println("ℹ️ Advanced programs not available for this model.");
                            case "7" -> dryer.status();
                            case "8" -> {
                                System.out.println("↩️ Back to device menu.");
                                return;
                            }
                        }
                    }
                }
                default -> System.out.println("❌ Invalid option. Please try again.");
            }
        }
    }
}
