package devicesTests;

import devices.DeviceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTypeTest {

    @Test
    void whenValidInputFromStringCalled_shouldReturnMatchingEnum() {
        assertEquals(DeviceType.LIGHT, DeviceType.fromString("light"));
        assertEquals(DeviceType.THERMOSTAT, DeviceType.fromString("ThErMoStAt"));
        assertEquals(DeviceType.WASHING_MACHINE, DeviceType.fromString("WASHING_MACHINE"));
    }

    @Test
    void whenInvalidInputFromStringCalled_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DeviceType.fromString("TOASTER");
        });
        assertTrue(exception.getMessage().contains("Unsupported device type"));
    }

    @Test
    void whenValidTypeStringPassedToIsValidType_shouldReturnTrue() {
        assertTrue(DeviceType.isValidType("sensor"));
        assertTrue(DeviceType.isValidType("GENERIC"));
        assertTrue(DeviceType.isValidType("dryer"));
    }

    @Test
    void whenInvalidTypeStringPassedToIsValidType_shouldReturnFalse() {
        assertFalse(DeviceType.isValidType("coffeeMaker"));
        assertFalse(DeviceType.isValidType(""));
        assertFalse(DeviceType.isValidType("   "));
    }

    @Test
    void toStringShouldReturnExactEnumName() {
        assertEquals("GENERIC", DeviceType.GENERIC.toString());
        assertEquals("SENSOR", DeviceType.SENSOR.toString());
    }
}
