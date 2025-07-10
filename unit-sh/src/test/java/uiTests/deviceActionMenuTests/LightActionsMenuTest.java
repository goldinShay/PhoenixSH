package uiTests.deviceActionMenuTests;

import devices.Light;
import org.junit.jupiter.api.*;
import ui.deviceActionMenu.LightActionsMenu;
import ui.AutoOpController;

import java.io.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LightActionsMenuTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(System.in); // reset input stream
    }

    private Scanner createMockScanner(String... inputs) {
        String joined = String.join("\n", inputs);
        return new Scanner(new ByteArrayInputStream(joined.getBytes()));
    }

    @Test
    void whenNonLightDevicePassed_thenWarnAndExit() {
        LightActionsMenu.show(mock(devices.Device.class), createMockScanner());
        assertTrue(outContent.toString().contains("⚠️ This menu is only for basic Light devices."));
    }

    @Test
    void whenUserSelectsTurnOn_thenLightIsTurnedOn() {
        Light mockLight = mock(Light.class);
        Scanner scanner = createMockScanner("1", "4", "");

        LightActionsMenu.show(mockLight, scanner);

        verify(mockLight).turnOn();
        verify(mockLight, atLeastOnce()).isOn();
        verify(mockLight, atLeastOnce()).isAutomationEnabled();
    }
    @Test
    void whenUserSelectsTurnOff_thenLightIsTurnedOff() {
        Light mockLight = mock(Light.class);
        Scanner scanner = createMockScanner("2", "4", "");

        LightActionsMenu.show(mockLight, scanner);

        verify(mockLight).turnOff();
        verify(mockLight, atLeastOnce()).isOn();
        verify(mockLight, atLeastOnce()).isAutomationEnabled();
    }


    @Test
    void whenInvalidOptionSelected_thenDisplayErrorMessage() {
        Light mockLight = mock(Light.class);
        Scanner scanner = createMockScanner("0", "4", ""); // invalid then back

        LightActionsMenu.show(mockLight, scanner);

        assertTrue(outContent.toString().contains("❌ Invalid option."));
    }

    @Test
    void whenOptionFourSelected_thenExitMenu() {
        Light mockLight = mock(Light.class);
        Scanner scanner = createMockScanner("4", "", "");

        LightActionsMenu.show(mockLight, scanner);

        assertTrue(outContent.toString().contains("↩️ Back to device menu."));
    }

    @Test
    void whenLightHasKnownState_thenDisplayCorrectStatus() {
        Light mockLight = mock(Light.class);
        when(mockLight.isOn()).thenReturn(true);
        when(mockLight.isAutomationEnabled()).thenReturn(false);

        LightActionsMenu.show(mockLight, createMockScanner("4", ""));

        String output = outContent.toString();
        assertTrue(output.contains("Power: ON"));
        assertTrue(output.contains("Automation: DISABLED"));
    }
}
