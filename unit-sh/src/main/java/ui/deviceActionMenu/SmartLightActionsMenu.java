package ui.deviceActionMenu;

import devices.Device;
import devices.SmartLight;
import devices.actions.SmartLightAction;

import java.util.Scanner;

public class SmartLightActionsMenu {
    private static final Scanner scanner = new Scanner(System.in);

    public static void show(Device device) {
        if (!(device instanceof SmartLight light)) {
            System.out.println("⚠️ This menu is only for SmartLight devices.");
            return;
        }

        boolean isCalexA60E27 = "Calex A60E27".equalsIgnoreCase(light.getModel());

        while (true) {
            System.out.println("\n=== Smart Light Actions ===");
            System.out.println("Power: " + (light.isOn() ? "ON" : "OFF"));
            System.out.println("1 - Turn ON");
            System.out.println("2 - Turn OFF");

            if (isCalexA60E27) {
                System.out.println("3 - Set Custom Light Mode (RGB + Intensity)");
                System.out.println("4 - View Current Mode");
                System.out.println("5 - Back");
            } else {
                System.out.println("3 - Advanced Mode (Not available for this model)");
                System.out.println("4 - Back");
            }

            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> light.turnOn();
                case "2" -> light.turnOff();
                case "3" -> {
                    if (isCalexA60E27) {
                        System.out.print("Enter brightness (0-100): ");
                        int brightness = safeInt(scanner.nextLine().trim());

                        System.out.print("Enter Red (0-255): ");
                        int r = safeInt(scanner.nextLine().trim());

                        System.out.print("Enter Green (0-255): ");
                        int g = safeInt(scanner.nextLine().trim());

                        System.out.print("Enter Blue (0-255): ");
                        int b = safeInt(scanner.nextLine().trim());

                        SmartLightAction newMode = new SmartLightAction(brightness, r, g, b);
                        light.setLightMode(newMode);
                    } else {
                        System.out.println("ℹ️ This model does not support color/intensity settings.");
                    }
                }
                case "4" -> {
                    if (isCalexA60E27) {
                        var mode = light.getLightMode();
                        System.out.println(mode != null
                                ? "💡 Current Light Mode: " + mode
                                : "ℹ️ No custom light mode set yet.");
                    } else {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    }
                }
                case "5" -> {
                    if (isCalexA60E27) {
                        System.out.println("↩️ Back to device menu.");
                        return;
                    } else {
                        System.out.println("❌ Invalid option.");
                    }
                }
                default -> System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }

    private static int safeInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
