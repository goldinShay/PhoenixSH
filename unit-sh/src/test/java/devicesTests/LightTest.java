package devicesTests;

import devices.Device;
import devices.Light;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

public class LightTest {

    private Light testLight;

    @BeforeEach
    void setUp() {
        // 🧼 Clear registry to avoid duplicate ID exceptions
        Device.clearDeviceRegistryForTests();

        testLight = new Light(
                "LI001",
                "Basement_Studio_Test",
                Clock.systemDefaultZone(),
                false,
                1400.0,
                1400.0
        );
    }

    @Test
    void whenCreatedWithInitialStateFalse_lightShouldStartOff() {
        assertFalse(testLight.isOn(), "Light should be OFF when initialized with 'false'");
    }

    @Test
    void whenTurnOnIsCalled_lightShouldBeOn() {
        testLight.turnOn();
        assertTrue(testLight.isOn(), "Light should be ON after calling turnOn()");
    }

    @Test
    void whenTurnOffIsCalled_lightShouldBeOff() {
        testLight.turnOn();
        testLight.turnOff();
        assertFalse(testLight.isOn(), "Light should be OFF after calling turnOff()");
    }
}
