package devicesTests.actionsTests;

import devices.actions.SmartLightAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartLightActionTest {

    @Test
    void whenCreatedWithValidValues_thenStoresClampedCorrectly() {
        SmartLightAction action = new SmartLightAction("Test Mode", 85, 120, 200, 40);
        assertEquals("Test Mode", action.getLabel());
        assertEquals(85, action.getIntensity());
        assertEquals(120, action.getRed());
        assertEquals(200, action.getGreen());
        assertEquals(40, action.getBlue());
    }

    @Test
    void whenCreatedWithoutLabel_thenUsesUnnamedMode() {
        SmartLightAction action = new SmartLightAction(70, 10, 20, 30);
        assertEquals("Unnamed Mode", action.getLabel());
    }

    @Test
    void whenValuesExceedBounds_thenTheyAreClamped() {
        SmartLightAction action = new SmartLightAction("Clamped", 200, -50, 300, 500);
        assertEquals(100, action.getIntensity()); // Max clamp
        assertEquals(0, action.getRed());         // Min clamp
        assertEquals(255, action.getGreen());     // Max clamp
        assertEquals(255, action.getBlue());      // Max clamp
    }

    @Test
    void whenCallingToString_thenReturnsFormattedDescription() {
        SmartLightAction action = new SmartLightAction("Evening", 75, 120, 110, 100);
        assertEquals("Evening | 75% brightness | RGB(120, 110, 100)", action.toString());
    }

    @Test
    void whenCallingDim_thenReturnsNewDimmedInstance() {
        SmartLightAction original = new SmartLightAction("Work Mode", 80, 100, 100, 100);
        SmartLightAction dimmed = original.dim();

        assertEquals("Work Mode (Dimmed)", dimmed.getLabel());
        assertEquals(72, dimmed.getIntensity());
        assertEquals(original.getRed(), dimmed.getRed());
        assertEquals(original.getGreen(), dimmed.getGreen());
        assertEquals(original.getBlue(), dimmed.getBlue());
    }

    @Test
    void whenCallingBrighten_thenReturnsNewBrightenedInstance() {
        SmartLightAction original = new SmartLightAction("Work Mode", 90, 100, 100, 100);
        SmartLightAction bright = original.brighten();

        assertEquals("Work Mode (Bright)", bright.getLabel());
        assertEquals(99, bright.getIntensity());
        assertEquals(original.getRed(), bright.getRed());
        assertEquals(original.getGreen(), bright.getGreen());
        assertEquals(original.getBlue(), bright.getBlue());
    }

    @Test
    void whenIntensityAlreadyNearMax_thenBrightenDoesNotExceed100() {
        SmartLightAction intense = new SmartLightAction("Max", 100, 50, 50, 50);
        assertEquals(100, intense.brighten().getIntensity());
    }

    @Test
    void whenIntensityAlreadyLow_thenDimDoesNotGoBelow10() {
        SmartLightAction low = new SmartLightAction("Low", 5, 10, 10, 10);
        assertEquals(10, low.dim().getIntensity());
    }
}
