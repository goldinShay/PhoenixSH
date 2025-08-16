package ui.deviceActionMenu;

import devices.SmartLight;
import devices.actions.SmartLightColorMode;
import devices.actions.SmartLightEffect;
import devices.actions.SmartLightRgbEditor;
import autoOp.AutoOpController;
import utils.Input;

public class SmartLightActionsMenu {

    public static void show(SmartLight smart) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Smart Light Actions ===");

            // 🎛 Dynamic status header
            String effectStatus = smart.getLiteFx() != SmartLightEffect.NONE
                    ? "🎞️ Effect: " + smart.getLiteFx().name()
                    : "🎨 Color Mode: " + smart.getColorMode().getLabel();

            System.out.printf("Power: %s | Automation: %s | %s%n",
                    smart.isOn() ? "ON" : "OFF",
                    smart.isAutomationEnabled() ? "ENABLED" : "DISABLED",
                    effectStatus);

            // ✅ Menu options
            System.out.println("""
                \n    1 - Turn ON
                2 - Turn OFF
                3 - AutoOp 
                4 - Change Color Mode 
                5 - Apply LiteFx
                6 - Edit RGB
                7 - Back
            """);

            int choice = Input.getInt("Select: ");
            switch (choice) {
                case 1 -> smart.turnOn();
                case 2 -> smart.turnOff();
                case 3 -> AutoOpController.display(smart);
                case 4 -> selectColorMode(smart);
                case 5 -> applyLiteFx(smart);
                case 6 -> SmartLightRgbEditor.launchRgbEditor(smart);
                case 7 -> back = true;
                default -> System.out.println("❌ Invalid choice.");
            }
        }
    }

    private static void selectColorMode(SmartLight smart) {
        SmartLightColorMode[] modes = SmartLightColorMode.values();
        System.out.println("\n🎨 Available Color Modes:");
        for (int i = 0; i < modes.length; i++) {
            System.out.printf("%d - %s%n", i + 1, modes[i].getLabel());
        }

        int choice = Input.getInt("Pick mode: ", 1, modes.length);
        SmartLightColorMode selectedMode = modes[choice - 1];
        smart.setColorMode(selectedMode);
        smart.setLiteFx(SmartLightEffect.NONE); // FX resets on mode change
        System.out.printf("✅ Color mode set to: %s%n", selectedMode.getLabel());
    }

    private static void applyLiteFx(SmartLight smart) {
        if (!smart.supportsCustomMode()) {
            System.out.println("⚠️ FX not supported by this model.");
            return;
        }

        SmartLightEffect[] effects = SmartLightEffect.values();
        System.out.println("\n🌠 Available LiteFx:");
        for (int i = 0; i < effects.length; i++) {
            System.out.printf("%d - %s%n", i + 1, effects[i].getDescription());
        }

        int choice = Input.getInt("Pick effect: ", 1, effects.length);
        SmartLightEffect selectedFx = effects[choice - 1];
        smart.setLiteFx(selectedFx);
    }
}