package utils;

// 📁 devices.actions or 📁 ui.deviceActionMenu (either works fine)

import devices.SmartLight;
import devices.actions.SmartLightEffect;

public class SmartLightEffectManager {

    public static void handleEffectToggle(SmartLight smart) {
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
}
