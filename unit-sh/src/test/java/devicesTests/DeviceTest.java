package devicesTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTest {

    private Device device;

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();

        device = new TestableDevice(
                "DEV001",
                "Test_Device",
                Clock.systemDefaultZone(),
                false
        );
    }

    @Test
    void whenCreatedWithInitialStateFalse_deviceShouldBeOff() {
        assertFalse(device.isOn());
    }

    @Test
    void whenTurnOnIsCalled_deviceShouldBeOn() {
        device.turnOn();
        assertTrue(device.isOn());
    }

    @Test
    void whenTurnOffIsCalled_deviceShouldBeOff() {
        device.turnOn(); // Start ON
        device.turnOff();
        assertFalse(device.isOn());
    }

    @Test
    void whenSetAutoOnThresholdCalled_autoOnValueShouldUpdate() {
        device.setAutoOnThreshold(2345.0, true);
        assertEquals(2345.0, device.getAutoOnThreshold());
    }

    @Test
    void whenSetAutoOffThresholdCalled_autoOffValueShouldUpdate() {
        device.setAutoOffThreshold(123.0);
        assertEquals(123.0, device.getAutoOffThreshold());
    }

    @Test
    void whenSetAutomationSensorIdCalled_itShouldStoreTheValue() {
        device.setAutomationSensorId("SENSOR_X");
        assertEquals("SENSOR_X", device.getAutomationSensorId());
    }

    @Test
    void whenSetOffThresholdUsedTrue_itShouldReflectState() {
        assertFalse(device.isOffThresholdUsed());
        device.setOffThresholdUsed(true);
        assertTrue(device.isOffThresholdUsed());
    }

    // Used only for testing the abstract Device class
    class TestableDevice extends Device {
        public TestableDevice(String id, String name, Clock clock, boolean initialState) {
            super(id, name, DeviceType.LIGHT, clock, 1500.0, 1500.0);
            setState(initialState ? "ON" : "OFF");
        }

        @Override
        public List<String> getAvailableActions() {
            return List.of("on", "off");
        }

        @Override
        public void simulate(String action) {
            if ("on".equalsIgnoreCase(action)) turnOn();
            if ("off".equalsIgnoreCase(action)) turnOff();
        }
    }

}
