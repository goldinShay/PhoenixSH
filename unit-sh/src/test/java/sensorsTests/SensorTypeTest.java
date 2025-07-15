package sensorsTests;

import org.junit.jupiter.api.Test;
import sensors.SensorType;

import static org.junit.jupiter.api.Assertions.*;

public class SensorTypeTest {

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertEquals(SensorType.LIGHT, SensorType.valueOf("LIGHT"));
        assertEquals(SensorType.CAMERA, SensorType.valueOf("CAMERA"));
        assertEquals(SensorType.SOFTENER_LEVEL, SensorType.valueOf("SOFTENER_LEVEL"));
    }

    @Test
    void valueOf_shouldBeCaseSensitive() {
        assertThrows(IllegalArgumentException.class, () -> SensorType.valueOf("motion"));  // lowercase fails
    }

    @Test
    void values_shouldIncludeExpectedSensorCategories() {
        assertTrue(SensorType.valueOf("AIR_QUALITY") instanceof SensorType);
        assertTrue(SensorType.valueOf("DEBUG") instanceof SensorType);
    }

    @Test
    void enumShouldContainExpectedTotalTypes() {
        assertEquals(23, SensorType.values().length);
    }
}
