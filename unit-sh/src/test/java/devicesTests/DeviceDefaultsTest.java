package devicesTests;

import devices.DeviceDefaults;
import devices.DeviceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceDefaultsTest {

    @Test
    void testAutoOnDefaultsForEachType() {
        assertEquals(1024.0, DeviceDefaults.getDefaultAutoOn(DeviceType.LIGHT));
        assertEquals(23.0, DeviceDefaults.getDefaultAutoOn(DeviceType.THERMOSTAT));
        assertEquals(0.0, DeviceDefaults.getDefaultAutoOn(DeviceType.DRYER));
        assertEquals(0.0, DeviceDefaults.getDefaultAutoOn(DeviceType.WASHING_MACHINE));
        assertEquals(1050.0, DeviceDefaults.getDefaultAutoOn(DeviceType.UNKNOWN)); // fallback
    }

    @Test
    void testAutoOffDefaultsForEachType() {
        assertEquals(1050.0, DeviceDefaults.getDefaultAutoOff(DeviceType.LIGHT));
        assertEquals(27.0, DeviceDefaults.getDefaultAutoOff(DeviceType.THERMOSTAT));
        assertEquals(0.0, DeviceDefaults.getDefaultAutoOff(DeviceType.DRYER));
        assertEquals(0.0, DeviceDefaults.getDefaultAutoOff(DeviceType.WASHING_MACHINE));
        assertEquals(1080.0, DeviceDefaults.getDefaultAutoOff(DeviceType.UNKNOWN)); // fallback
    }
}
