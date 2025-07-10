package uiTests.deviceActionMenuTests;

import devices.Device;
import devices.WashingMachine;
import devices.actions.WashingMachineAction;
import org.junit.jupiter.api.*;
import ui.deviceActionMenu.WasherActionsMenu;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WasherActionsMenuTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        outContent.reset();
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void feedMockInput(String... inputs) {
        List<String> allInputs = new ArrayList<>(List.of(inputs));
        allInputs.add(""); // buffer line to avoid scanner exhaustion
        String joined = String.join("\n", allInputs);
        ByteArrayInputStream inStream = new ByteArrayInputStream(joined.getBytes());
        System.setIn(inStream);
    }

    // 1️⃣ Non-washer device
    @Test
    void whenDeviceIsNotWashingMachine_thenShowWarningAndExit() {
        feedMockInput("1");
        WasherActionsMenu.show(mock(Device.class));

        assertTrue(outContent.toString().contains("⚠️ This menu is only for Washing Machines."));
    }

    // 2️⃣ Turn ON
    @Test
    void whenOptionOneSelected_thenTurnOnWasher() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("1", "7");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).turnOn();
    }

    // 3️⃣ Turn OFF
    @Test
    void whenOptionTwoSelected_thenTurnOffWasher() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("2", "7");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).turnOff();
    }

    // 4️⃣ Start Program
    @Test
    void whenOptionThreeSelected_thenStartProgram() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("3", "7");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).start();
    }

    // 5️⃣ Stop Program
    @Test
    void whenOptionFourSelected_thenStopProgram() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("4", "7");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).stop();
    }

    // 6️⃣ Non-flagship Advanced Program
    @Test
    void whenOptionSixSelected_onBasicModel_thenShowUnavailable() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("6", "7");

        WasherActionsMenu.show(mockWasher);

        assertTrue(outContent.toString().contains("ℹ️ Advanced programs not supported on this model."));
    }

    // 7️⃣ Flagship Quick Wash
    @Test
    void whenFlagshipSelectsQuickWash_thenApplyMode() {
        WashingMachine mockWasher = baseWasher(true);
        feedMockInput("6", "9");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).setMode(WashingMachineAction.QUICK_WASH);
        assertTrue(outContent.toString().contains("🚿 Quick Wash started."));
    }

    // 8️⃣ Flagship Heavy Duty
    @Test
    void whenFlagshipSelectsHeavyDuty_thenApplyModeAndStay() {
        WashingMachine mockWasher = baseWasher(true);
        feedMockInput("7", "9");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).setMode(WashingMachineAction.HEAVY_DUTY);
        assertTrue(outContent.toString().contains("💪 Heavy Duty started."));
    }

    // 9️⃣ Flagship Rinse and Spin
    @Test
    void whenFlagshipSelectsRinseSpin_thenApplyMode() {
        WashingMachine mockWasher = baseWasher(true);
        feedMockInput("8", "9");

        WasherActionsMenu.show(mockWasher);

        verify(mockWasher).setMode(WashingMachineAction.RINSE_AND_SPIN);
        String output = outContent.toString();
        assertTrue(output.contains("🔄"));
        assertTrue(output.contains("activated"));
        assertTrue(output.contains(WashingMachineAction.RINSE_AND_SPIN.getLabel()));
    }

    // 🔟 Basic Model Back Option
    @Test
    void whenBasicModelSelectsBack_thenExitMenu() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("7");

        WasherActionsMenu.show(mockWasher);

        assertTrue(outContent.toString().contains("↩️ Back to device menu."));
    }

    // 🧁 Flagship Back Option
    @Test
    void whenFlagshipSelectsBack_thenExitMenu() {
        WashingMachine mockWasher = baseWasher(true);
        feedMockInput("9");

        WasherActionsMenu.show(mockWasher);

        assertTrue(outContent.toString().contains("↩️ Back to device menu."));
    }

    // ❌ Invalid Option
    @Test
    void whenInvalidOptionSelected_thenShowErrorMessage() {
        WashingMachine mockWasher = baseWasher(false);
        feedMockInput("99", "7");

        WasherActionsMenu.show(mockWasher);

        assertTrue(outContent.toString().contains("❌ Invalid option. Please try again."));
    }

    // 🧪 Helper: Base washer setup
    private WashingMachine baseWasher(boolean isFlagship) {
        WashingMachine mockWasher = mock(WashingMachine.class);
        when(mockWasher.getModel()).thenReturn(isFlagship ? "BWM14025" : "LG1000");
        when(mockWasher.isOn()).thenReturn(true);
        when(mockWasher.isRunning()).thenReturn(false);
        when(mockWasher.isAutomationEnabled()).thenReturn(true);
        return mockWasher;
    }
}
