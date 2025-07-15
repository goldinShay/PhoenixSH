package devicesTests;

import devices.Device;
import devices.Thermostat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

public class ThermostatTest {

    private Thermostat testThermostat;

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();

        testThermostat = new Thermostat(
                "TH001",
                "Bedroom_Thermostat_Test",
                Clock.systemDefaultZone(),
                false,
                21.0,
                27.0
        );
    }

    @Test
    void whenTurnOnIsCalled_thermostatShouldBeOn() {
        testThermostat.turnOn();
        assertTrue(testThermostat.isOn(), "Thermostat should be ON after calling turnOn()");
    }

    @Test
    void whenTurnOffIsCalled_thermostatShouldBeOff() {
        testThermostat.turnOn(); // Ensure it's ON first
        testThermostat.turnOff();
        assertFalse(testThermostat.isOn(), "Thermostat should be OFF after calling turnOff()");
    }

    @Test
    void whenCreatedWithInitialStateFalse_thermostatShouldStartOff() {
        assertFalse(testThermostat.isOn(), "Thermostat should be OFF when initialized with 'false'");
    }

    @Test
    void whenIncreaseUserTempCalled_userTempShouldIncrementByOne() {
        double initialTemp = testThermostat.getUserTemp();
        testThermostat.increaseUserTemp();
        assertEquals(initialTemp + 1.0, testThermostat.getUserTemp(), 0.01);
    }

    @Test
    void whenDecreaseUserTempCalled_userTempShouldDecrementByOne() {
        double initialTemp = testThermostat.getUserTemp();
        testThermostat.decreaseUserTemp();
        assertEquals(initialTemp - 1.0, testThermostat.getUserTemp(), 0.01);
    }
}
