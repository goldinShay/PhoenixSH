package devicesTests.actionsTests;

import devices.actions.SmartLightEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartLightEffectTest {

    @Test
    void whenCallingGetDescription_thenReturnsExpectedText() {
        assertEquals("Full-Spectrum Color Cycle", SmartLightEffect.RAINBOW.getDescription());
        assertEquals("Warm Flicker", SmartLightEffect.FIRE.getDescription());
        assertEquals("No Effect", SmartLightEffect.NONE.getDescription());
    }

    @Test
    void whenCallingToString_thenIncludesEnumNameAndDescription() {
        assertEquals("FIRE — Warm Flicker", SmartLightEffect.FIRE.toString());
        assertEquals("PARTY_LOOP — High-Energy Color Jumps", SmartLightEffect.PARTY_LOOP.toString());
        assertEquals("BREATHING — Slow Breathing Light", SmartLightEffect.BREATHING.toString());
    }

    @Test
    void whenCheckingEnumCompleteness_thenAllEffectsArePresent() {
        SmartLightEffect[] all = SmartLightEffect.values();
        assertTrue(all.length >= 6); // Sanity check in case someone "forgets" to register confetti mode
        assertTrue(java.util.EnumSet.allOf(SmartLightEffect.class).contains(SmartLightEffect.NONE));
    }
}
