package ui.deviceActionMenu;

import devices.Device;
import devices.WashingMachine;
import devices.actions.WashingMachineAction;
import devices.actions.actionSimulator.WasherCycleSimulator;
import devices.actions.advancedActions.WashActionsLGTwin;
import autoOp.AutoOpController;

import java.util.List;
import java.util.Scanner;

public class WasherActionsMenu {

    public static void show(Device device) {
        if (!(device instanceof WashingMachine washer)) {
            System.out.println("⚠️ This menu is only for Washing Machines.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String brand = washer.getBrand();
        String model = washer.getModel();

        boolean isLGTwinWash = WashActionsLGTwin.isCompatible(brand, model);
        List<WashingMachineAction> availablePrograms = isLGTwinWash
                ? WashActionsLGTwin.getAvailablePrograms()
                : List.of(); // fallback for unsupported models

        WashingMachineAction defaultProgram = WashingMachineAction.ECO_WASH;

        while (true) {
            System.out.println("\n=== Washing Machine Actions ===");
            System.out.println("Power: " + (washer.isOn() ? "ON" : "OFF") +
                    " | Running: " + (washer.isRunning() ? "YES" : "NO"));
            System.out.println("Automation: " + (washer.isAutomationEnabled() ? "ENABLED" : "DISABLED"));

            System.out.println("1 - Turn ON");
            System.out.println("2 - Turn OFF");
            System.out.println("3 - Start Program (" + defaultProgram.getLabel() + ")");
            System.out.println("4 - Stop Program");
            System.out.println("5 - AutoOp");

            if (!availablePrograms.isEmpty()) {
                for (int i = 0; i < availablePrograms.size(); i++) {
                    WashingMachineAction action = availablePrograms.get(i);
                    int duration = WashActionsLGTwin.getEstimatedDuration(action);
                    System.out.printf("%d - %s (%d min)%n", 6 + i, action.getLabel(), duration);
                }
                System.out.println((6 + availablePrograms.size()) + " - Back");
            } else {
                System.out.println("6 - Advanced Programs (Not available on this model)");
                System.out.println("7 - Back");
            }

            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> washer.turnOn();
                case "2" -> washer.turnOff();
                case "3" -> {
                    washer.setMode(defaultProgram);
                    System.out.println("🌱 " + defaultProgram.getLabel() + " started.");
                    WasherCycleSimulator.simulateCycle(defaultProgram);
                }
                case "4" -> washer.stop();
                case "5" -> AutoOpController.display(washer);
                default -> {
                    int offset = 6;
                    int backOption = offset + availablePrograms.size();

                    if (!availablePrograms.isEmpty()) {
                        try {
                            int choice = Integer.parseInt(input);
                            if (choice >= offset && choice < backOption) {
                                WashingMachineAction selected = availablePrograms.get(choice - offset);
                                washer.setMode(selected);
                                System.out.println("🌀 " + selected.getLabel() + " activated.");
                                WasherCycleSimulator.simulateCycle(selected);
                            } else if (choice == backOption) {
                                System.out.println("↩️ Back to device menu.");
                                return;
                            } else {
                                System.out.println("❌ Invalid option.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Invalid input. Please enter a number.");
                        }
                    } else if ("6".equals(input)) {
                        System.out.println("ℹ️ Advanced programs not supported on this model.");
                    } else if ("7".equals(input)) {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    } else {
                        System.out.println("❌ Invalid option. Please try again.");
                    }
                }
            }
        }
    }
}
