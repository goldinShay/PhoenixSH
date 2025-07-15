package sensorsTests;

import org.junit.jupiter.api.Test;
import sensors.SensorAction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SensorActionTest {

    @Test
    void WhenCallingValueOf_shouldReturnCorrectEnumForValidName() {
        assertEquals(SensorAction.ON, SensorAction.valueOf("ON"));
        assertEquals(SensorAction.STREAM, SensorAction.valueOf("STREAM"));
        assertEquals(SensorAction.CALIBRATE, SensorAction.valueOf("CALIBRATE"));
    }

    @Test
    void WhenCallingValues_shouldIncludeAllDefinedActions() {
        assertEquals(11, SensorAction.values().length);
        assertTrue(List.of(SensorAction.values()).contains(SensorAction.SEND_ALERT));
        assertTrue(List.of(SensorAction.values()).contains(SensorAction.SIMULATE));
    }

    @Test
    void WhenCallingValueOf_withInvalidName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> SensorAction.valueOf("SCAN"));
        assertThrows(IllegalArgumentException.class, () -> SensorAction.valueOf("fire"));
    }

    @Test
    void enumNames_shouldBeUppercaseByConvention() {
        for (SensorAction action : SensorAction.values()) {
            assertTrue(action.name().equals(action.name().toUpperCase()));
        }
    }
}
