// ui/AutoOpController.java
package autoOp;

import devices.Device;

import java.util.Scanner;

public class AutoOpController {

    private static final Scanner defaultScanner = new Scanner(System.in);

    // 👇 New overload for test input
    public static void display(Device device) {
        display(device, defaultScanner);
    }

    // 👇 Test-friendly variant
    public static void display(Device device, Scanner scanner) {
        System.out.println("\n=== AutoOp Settings ===");
        System.out.println("Current AutoOp: " + (device.isAutomationEnabled() ? "🟢 ENABLED" : "🔴 DISABLED"));
        System.out.println("Linked Sensor: " + (device.getAutomationSensorId() != null ? device.getAutomationSensorId() : "None"));
        System.out.println("1 - ENABLE AutoOp");
        System.out.println("2 - DISABLE AutoOp");
        System.out.println("3 - Back");

        System.out.print("Choose option: ");
        String input = scanner.nextLine().trim();

        switch (input) {
            case "1" -> {
                AutoOpLinker.promptAndLink(device, scanner);
            }
            case "2" -> AutoOpUnlinker.disable(device);
            case "3" -> System.out.println("↩️ Back to device menu.");
            default -> System.out.println("❌ Invalid choice.");
        }
    }

}

