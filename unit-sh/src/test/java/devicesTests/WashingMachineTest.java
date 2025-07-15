package devicesTests;

import devices.Device;
import devices.WashingMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

public class WashingMachineTest {

    private WashingMachine testWashingMachine;

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();

        testWashingMachine = new WashingMachine(
                "WM001",
                "LaundryRoom_Test",
                Clock.systemDefaultZone(),
                false,
                1200.0,
                1200.0
        );
    }

    @Test
    void whenCreatedWithInitialStateFalse_washingMachineShouldStartOff() {
        assertFalse(testWashingMachine.isOn());
    }

    @Test
    void whenTurnOnIsCalled_washingMachineShouldBeOn() {
        testWashingMachine.turnOn();
        assertTrue(testWashingMachine.isOn());
    }

    @Test
    void whenTurnOffIsCalled_washingMachineShouldBeOff() {
        testWashingMachine.turnOn();
        testWashingMachine.turnOff();
        assertFalse(testWashingMachine.isOn());
    }
}
