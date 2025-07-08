package ui.deviceActionMenu;

import devices.SmartLight;
import devices.actions.SmartLightAction;
import devices.actions.SmartLightEffect;
import devices.actions.SmartLightRgbEditor;
import utils.Input;

public class SmartLightActionsMenu {

    public static void show(SmartLight smart) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Smart Light Actions ===");

            // Show dynamic status header
            String effectStatus = smart.getEffect() != SmartLightEffect.NONE
                    ? "🎞️ Effect: " + smart.getEffect().name()
                    : smart.getLightMode() != null
                    ? String.format("🎨 Color: %s (Intensity: %d%%)",
                    smart.getLightMode().getLabel(),
                    smart.getLightMode().getIntensity())
                    : "🎨 Color: None";

            System.out.printf("Power: %s | Automation: %s | %s%n",
                    smart.isOn() ? "ON" : "OFF",
                    smart.isAutomationEnabled() ? "ENABLED" : "DISABLED",
                    effectStatus);

            // ✅ Menu options
            System.out.println("""
                \n1 - Turn ON
                2 - Turn OFF
                3 - Toggle Automation
                4 - Toggle Effect
                5 - Set Light Mode
                6 - Adjust RGB Channels
                7 - Schedule
                8 - Back
                """);

            int choice = Input.getInt("Select: ");
            switch (choice) {
                case 1 -> smart.turnOn();
                case 2 -> smart.turnOff();
                case 3 -> toggleAutomation(smart);
                case 4 -> utils.SmartLightEffectManager.handleEffectToggle(smart);
                case 5 -> selectLightMode(smart);
                case 6 -> SmartLightRgbEditor.launchRgbEditor(smart);
                case 7 -> System.out.println("📅 Schedule setup coming soon...");
                case 8 -> back = true;
                default -> System.out.println("❌ Invalid choice.");
            }
        }
    }

    private static void toggleAutomation(SmartLight smart) {
        smart.setAutomationEnabled(!smart.isAutomationEnabled());
        System.out.printf("🔁 Automation %s.%n", smart.isAutomationEnabled() ? "ENABLED" : "DISABLED");
    }

    private static void toggleEffect(SmartLight smart) {
        if (smart.getEffect() == SmartLightEffect.NONE) {
            System.out.println("\n🌈 Available Effects:");
            SmartLightEffect[] effects = SmartLightEffect.values();
            for (int i = 0; i < effects.length; i++) {
                if (effects[i] != SmartLightEffect.NONE)
                    System.out.printf("%d - %s%n", i + 1, effects[i]);
            }

            int choice = Input.getInt("Pick effect: ", 1, effects.length - 1);
            smart.applyEffect(effects[choice - 1]);
        } else {
            smart.applyEffect(SmartLightEffect.NONE);
            System.out.println("🛑 Effect disabled.");
        }
    }

    private static void selectLightMode(SmartLight smart) {
        if (!smart.supportsCustomMode()) {
            System.out.println("⚠️ This model does not support static light modes.");
            return;
        }

        SmartLightAction custom = new SmartLightAction("CUSTOM", 100, 100, 100, 100);
        smart.setLightMode(custom);
        smart.applyEffect(SmartLightEffect.NONE);
        smart.turnOn();
        System.out.println("✨ CUSTOM mode applied (100,100,100)");
    }

    private static void adjustRGB(SmartLight smart) {
        SmartLightAction mode = smart.getLightMode();
        if (mode == null) {
            System.out.println("⚠️ No light mode set. Applying CUSTOM first.");
            selectLightMode(smart);
            mode = smart.getLightMode();
        }

        int r = mode.getRed();
        int g = mode.getGreen();
        int b = mode.getBlue();
        int intensity = mode.getIntensity();

        boolean back = false;
        while (!back) {
            System.out.printf("\n🎛️ RGB Control — Current: R:%d G:%d B:%d | Intensity: %d%%%n", r, g, b, intensity);
            System.out.println("""
                1 - Set Red
                2 - Set Green
                3 - Set Blue
                4 - Increase Intensity
                5 - Decrease Intensity
                6 - Apply
                7 - Back
                """);

            int choice = Input.getInt("Choice: ");
            switch (choice) {
                case 1 -> r = getChannelValue("Red");
                case 2 -> g = getChannelValue("Green");
                case 3 -> b = getChannelValue("Blue");
                case 4 -> intensity = Math.min(100, intensity + 10);
                case 5 -> intensity = Math.max(10, intensity - 10);
                case 6 -> {
                    smart.setLightMode(new SmartLightAction("CUSTOM", intensity, r, g, b));
                    smart.turnOn();
                    System.out.println("✅ Custom color applied.");
                }
                case 7 -> back = true;
                default -> System.out.println("❌ Invalid.");
            }
        }
    }

    private static int getChannelValue(String channel) {
        return Input.getInt("Enter " + channel + " (0-100): ", 0, 100);
    }
}
