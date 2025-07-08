package devices.actions;

import devices.SmartLight;
import utils.Input;

public class SmartLightRgbEditor {

    public static void launchRgbEditor(SmartLight smart) {
        SmartLightAction current = smart.getLightMode();

        if (current == null) {
            System.out.println("⚠️ No light mode set. Applying CUSTOM(100,100,100)");
            current = new SmartLightAction("CUSTOM", 100, 100, 100, 100);
            smart.setLightMode(current);
            smart.applyEffect(SmartLightEffect.NONE); // Kill any animation
            smart.turnOn();
        }

        int r = current.getRed();
        int g = current.getGreen();
        int b = current.getBlue();
        int intensity = current.getIntensity();

        boolean back = false;
        while (!back) {
            System.out.printf("\n🎛 RGB Editor — R:%d G:%d B:%d | Intensity: %d%%%n", r, g, b, intensity);
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
                    SmartLightAction custom = new SmartLightAction("CUSTOM", intensity, r, g, b);
                    smart.setLightMode(custom);
                    smart.turnOn();
                    System.out.println("✅ RGB settings applied.");
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
