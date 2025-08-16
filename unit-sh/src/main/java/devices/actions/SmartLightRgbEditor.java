package devices.actions;

import devices.SmartLight;
import utils.Input;

public class SmartLightRgbEditor {

    public static void launchRgbEditor(SmartLight smart) {
        SmartLightColorMode currentMode = smart.getColorMode();

        int r = currentMode.getRed();
        int g = currentMode.getGreen();
        int b = currentMode.getBlue();
        int intensity = 100; // You can store intensity elsewhere if needed

        boolean back = false;
        while (!back) {
            System.out.printf("\n🎛 RGB Editor — R:%d G:%d B:%d | Intensity: %d%% | Mode: %s%n",
                    r, g, b, intensity, currentMode.getLabel());

            System.out.println("""
            1 - Set Red
            2 - Set Green
            3 - Set Blue
            4 - + Intensity
            5 - – Intensity
            6 - Apply Changes
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
                    SmartLightColorMode matched = SmartLightColorMode.matchColorMode(r, g, b);
                    smart.setColorMode(matched);

                    if (matched.isCustom()) {
                        System.out.println("🔧 Custom RGB applied.");
                    } else {
                        System.out.println("✅ Matched preset: " + matched.getLabel());
                    }

                    smart.turnOn();
                    // Intensity handling can go here if needed
                }
                case 7 -> back = true;
                default -> System.out.println("❌ Invalid choice.");
            }
        }
    }

    private static int getChannelValue(String channel) {
        return Input.getInt("Enter " + channel + " value (0-100): ", 0, 100);
    }

}
