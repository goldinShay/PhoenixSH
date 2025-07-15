package devicesTests;

import devices.Device;
import devices.Dryer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

public class DryerTest {

    private Dryer testDryer;

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();

        testDryer = new Dryer(
                "DR001",
                "BasementDryer_Test",
                Clock.systemDefaultZone(),
                false,
                1300.0,
                1300.0
        );
    }

    @Test
    void whenCreatedWithInitialStateFalse_dryerShouldStartOff() {
        assertFalse(testDryer.isOn());
    }

    @Test
    void whenTurnOnIsCalled_dryerShouldBeOn() {
        testDryer.turnOn();
        assertTrue(testDryer.isOn());
    }

    @Test
    void whenTurnOffIsCalled_dryerShouldBeOff() {
        testDryer.turnOn();
        testDryer.turnOff();
        assertFalse(testDryer.isOn());
    }
}
