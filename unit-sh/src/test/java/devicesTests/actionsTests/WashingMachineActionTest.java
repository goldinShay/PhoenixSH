package devicesTests.actionsTests;

import devices.actions.WashingMachineAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WashingMachineActionTest {

    @Test
    void whenGettingLabel_thenReturnsCorrectLabel() {
        assertEquals("Power On", WashingMachineAction.ON.getLabel());
        assertEquals("Rinse & Spin", WashingMachineAction.RINSE_AND_SPIN.getLabel());
    }

    @Test
    void whenGettingWashProgramProperties_thenReturnsCorrectTempAndSpin() {
        assertEquals(30, WashingMachineAction.QUICK_WASH.getWaterTemp());
        assertEquals(800, WashingMachineAction.QUICK_WASH.getSpinSpeed());

        assertEquals(60, WashingMachineAction.HEAVY_DUTY.getWaterTemp());
        assertEquals(1000, WashingMachineAction.HEAVY_DUTY.getSpinSpeed());
    }

    @Test
    void whenGettingPowerActionProperties_thenReturnsNegativeTempAndSpin() {
        assertEquals(-1, WashingMachineAction.OFF.getWaterTemp());
        assertEquals(-1, WashingMachineAction.OFF.getSpinSpeed());
    }

    @Test
    void whenCheckingIsProgram_thenReturnsTrueForProgramsOnly() {
        assertTrue(WashingMachineAction.HEAVY_DUTY.isProgram());
        assertTrue(WashingMachineAction.RINSE_AND_SPIN.isProgram());
        assertFalse(WashingMachineAction.ON.isProgram());
    }

    @Test
    void whenCheckingIsPowerAction_thenReturnsTrueForOnOffOnly() {
        assertTrue(WashingMachineAction.ON.isPowerAction());
        assertTrue(WashingMachineAction.OFF.isPowerAction());
        assertFalse(WashingMachineAction.QUICK_WASH.isPowerAction());
    }

    @Test
    void whenCallingToString_thenReturnsLabelOnly() {
        assertEquals("Quick Wash", WashingMachineAction.QUICK_WASH.toString());
        assertEquals("Power Off", WashingMachineAction.OFF.toString());
    }

    @Test
    void whenParsingFromName_thenReturnsCorrectEnum() {
        assertEquals(WashingMachineAction.HEAVY_DUTY, WashingMachineAction.fromString("HEAVY_DUTY"));
    }

    @Test
    void whenParsingFromLabel_thenReturnsCorrectEnum() {
        assertEquals(WashingMachineAction.ON, WashingMachineAction.fromString("Power On"));
        assertEquals(WashingMachineAction.RINSE_AND_SPIN, WashingMachineAction.fromString("Rinse & Spin"));
    }

    @Test
    void whenParsingInvalidInput_thenThrowsException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                WashingMachineAction.fromString("Delicates"));
        assertEquals("Unknown WashingMachineAction: Delicates", ex.getMessage());
    }
}
