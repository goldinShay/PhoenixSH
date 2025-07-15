package uiTests.deviceActionMenuTests;

import devices.Device;
import devices.Thermostat;
import org.junit.jupiter.api.*;
import ui.deviceActionMenu.ThermostatActionsMenu;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThermostatActionsMenuTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        outContent.reset(); // reset console output for clean test state
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // ✅ Now includes buffer line to prevent NoSuchElementException
    private void feedMockInput(String... inputs) {
        List<String> allInputs = new ArrayList<>(List.of(inputs));
        allInputs.add(""); // buffer input — absorbs extra nextLine()
        String joined = String.join("\n", allInputs);
        ByteArrayInputStream inStream = new ByteArrayInputStream(joined.getBytes());
        System.setIn(inStream);
    }

    @Test
    void whenDeviceIsNotThermostat_thenShowWarningAndExit() {
        feedMockInput("1");
        ThermostatActionsMenu.show(mock(Device.class));

        assertTrue(outContent.toString().contains("⚠️ This menu is only for Thermostats."));
    }

    @Test
    void whenOptionOneSelected_thenSetDefaultTemp() {
        Thermostat mockThermostat = mock(Thermostat.class);
        when(mockThermostat.getUserTemp()).thenReturn(22.0);
        feedMockInput("1", "4");

        ThermostatActionsMenu.show(mockThermostat);

        verify(mockThermostat).setUserTemp(25.0);
        assertTrue(outContent.toString().contains("🌡️ User temp reset to 25°C."));
    }

    @Test
    void whenOptionTwoSelected_thenIncreaseUserTemp() {
        Thermostat mockThermostat = mock(Thermostat.class);
        when(mockThermostat.getUserTemp()).thenReturn(23.0);
        feedMockInput("2", "4");

        ThermostatActionsMenu.show(mockThermostat);

        verify(mockThermostat).increaseUserTemp();
        verify(mockThermostat, atLeastOnce()).getUserTemp();
        assertTrue(outContent.toString().contains("🌡️ Increased to 23.0°C."));
    }

    @Test
    void whenOptionThreeSelected_thenDecreaseUserTemp() {
        Thermostat mockThermostat = mock(Thermostat.class);
        when(mockThermostat.getUserTemp()).thenReturn(21.0);
        feedMockInput("3", "4");

        ThermostatActionsMenu.show(mockThermostat);

        verify(mockThermostat).decreaseUserTemp();
        verify(mockThermostat, atLeastOnce()).getUserTemp();
        assertTrue(outContent.toString().contains("🌡️ Decreased to 21.0°C."));
    }

    @Test
    void whenOptionFourSelected_thenExitMenu() {
        Thermostat mockThermostat = mock(Thermostat.class);
        when(mockThermostat.getUserTemp()).thenReturn(25.0);
        feedMockInput("4");

        ThermostatActionsMenu.show(mockThermostat);

        assertTrue(outContent.toString().contains("↩️ Back to device control menu."));
    }

    @Test
    void whenInvalidOptionSelected_thenShowErrorMessage() {
        Thermostat mockThermostat = mock(Thermostat.class);
        when(mockThermostat.getUserTemp()).thenReturn(22.0);
        feedMockInput("99", "4");

        ThermostatActionsMenu.show(mockThermostat);

        assertTrue(outContent.toString().contains("❌ Invalid option. Please choose 1-4."));
    }
}
