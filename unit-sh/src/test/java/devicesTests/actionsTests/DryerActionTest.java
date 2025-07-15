package devicesTests.actionsTests;

import devices.actions.DryerAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DryerActionTest {

    @Test
    void whenGettingEcoDryLabel_thenReturnsCorrectValue() {
        assertEquals("EcoDry", DryerAction.ECO_DRY.getLabel());
    }

    @Test
    void whenGettingEcoDryDuration_thenReturnsCorrectMinutes() {
        assertEquals(45, DryerAction.ECO_DRY.getDurationMinutes());
    }

    @Test
    void whenCallingToString_thenReturnsFormattedLabelAndDuration() {
        assertEquals("EcoDry (45 mins)", DryerAction.ECO_DRY.toString());
        assertEquals("RapidDry (25 mins)", DryerAction.RAPID_DRY.toString());
        assertEquals("AntiCrease (15 mins)", DryerAction.ANTI_CREASE.toString());
    }

    @Test
    void whenParsingLabelCaseInsensitive_thenReturnsMatchingEnum() {
        assertEquals(DryerAction.ECO_DRY, DryerAction.fromLabel("ecodry"));
        assertEquals(DryerAction.RAPID_DRY, DryerAction.fromLabel("RAPIDDRY"));
        assertEquals(DryerAction.ANTI_CREASE, DryerAction.fromLabel("AntiCrease"));
    }

    @Test
    void whenParsingInvalidLabel_thenThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DryerAction.fromLabel("SuperDry"));
        assertEquals("Unknown DryerAction: SuperDry", ex.getMessage());
    }
}
