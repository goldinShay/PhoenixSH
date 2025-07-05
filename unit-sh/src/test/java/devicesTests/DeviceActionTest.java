package devicesTests;

import devices.DeviceAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceActionTest {

    @Test
    void whenInputIsOn_fromStringShouldReturnON() {
        assertEquals(DeviceAction.ON, DeviceAction.fromString("ON"));
    }

    @Test
    void whenInputIsTempPlus_fromStringShouldReturnTEMP_UP() {
        assertEquals(DeviceAction.TEMP_UP, DeviceAction.fromString("TEMP+"));
    }

    @Test
    void whenInputIsVolMinus_fromStringShouldReturnVOLUME_DOWN() {
        assertEquals(DeviceAction.VOLUME_DOWN, DeviceAction.fromString("vol-"));
    }

    @Test
    void whenInputIsInvalid_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DeviceAction.fromString("FLIP");
        });
        assertTrue(exception.getMessage().contains("Unknown action"));
    }
}
