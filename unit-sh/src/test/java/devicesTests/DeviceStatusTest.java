package devicesTests;

import devices.DeviceStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceStatusTest {

    @Test
    void whenCallingToString_shouldReturnLowercaseEnumName() {
        assertEquals("on", DeviceStatus.ON.toString());
        assertEquals("off", DeviceStatus.OFF.toString());
        assertEquals("paused", DeviceStatus.PAUSED.toString());
        assertEquals("running", DeviceStatus.RUNNING.toString());
        assertEquals("idle", DeviceStatus.IDLE.toString());
    }

    @Test
    void enumValueOf_shouldMatchExpectedStatus() {
        assertEquals(DeviceStatus.valueOf("ON"), DeviceStatus.ON);
        assertEquals(DeviceStatus.valueOf("IDLE"), DeviceStatus.IDLE);
    }
}
