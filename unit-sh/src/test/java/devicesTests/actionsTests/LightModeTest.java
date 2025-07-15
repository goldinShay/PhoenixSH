package devicesTests.actionsTests;

import devices.actions.LightMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LightModeTest {

    @Test
    void whenGettingRed_thenReturnsCorrectRedValue() {
        assertEquals(100, LightMode.CUSTOM.getRed());
        assertEquals(80, LightMode.COLD_WHITE.getRed());
        assertEquals(60, LightMode.STEEL_BLUE.getRed());
    }

    @Test
    void whenGettingGreen_thenReturnsCorrectGreenValue() {
        assertEquals(100, LightMode.CUSTOM.getGreen());
        assertEquals(90, LightMode.WARM_WHITE.getGreen());
        assertEquals(60, LightMode.SUNSET.getGreen());
    }

    @Test
    void whenGettingBlue_thenReturnsCorrectBlueValue() {
        assertEquals(100, LightMode.CUSTOM.getBlue());
        assertEquals(40, LightMode.NIGHT_MODE.getBlue());
        assertEquals(100, LightMode.DEEP_PURPLE.getBlue());
    }

    @Test
    void whenUsingAllEnums_thenAllValuesAreDistinctAndNonNegative() {
        for (LightMode mode : LightMode.values()) {
            assertTrue(mode.getRed() >= 0 && mode.getRed() <= 100, "Invalid red: " + mode);
            assertTrue(mode.getGreen() >= 0 && mode.getGreen() <= 100, "Invalid green: " + mode);
            assertTrue(mode.getBlue() >= 0 && mode.getBlue() <= 100, "Invalid blue: " + mode);
        }
    }
}
