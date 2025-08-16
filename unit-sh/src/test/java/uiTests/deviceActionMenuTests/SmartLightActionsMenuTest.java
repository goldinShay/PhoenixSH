package uiTests.deviceActionMenuTests;

import devices.SmartLight;
import devices.actions.SmartLightAction;
import devices.actions.SmartLightEffect;
import org.junit.jupiter.api.*;
import ui.deviceActionMenu.SmartLightActionsMenu;
import utils.Input;

import java.io.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmartLightActionsMenuTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Input.setScanner(new Scanner(System.in)); // restore utils.Input
    }

    private void feedMockInput(String... inputs) {
        String joined = String.join("\n", inputs);
        ByteArrayInputStream inStream = new ByteArrayInputStream(joined.getBytes());
        System.setIn(inStream);
        Input.setScanner(new Scanner(inStream));
    }

    @Test
    void whenTurnOnOptionSelected_thenLightTurnsOn() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("1", "8");

        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE); // 💥 Fix the crash
        when(mockLight.isAutomationEnabled()).thenReturn(true);        // Optional
        when(mockLight.isOn()).thenReturn(false);                      // Optional

        SmartLightActionsMenu.show(mockLight);

        verify(mockLight).turnOn();
        verify(mockLight, atLeastOnce()).isOn();
        verify(mockLight, atLeastOnce()).isAutomationEnabled();
    }

    @Test
    void whenTurnOffOptionSelected_thenLightTurnsOff() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("2", "8");

        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE); // 🩹 Stops NPE
        when(mockLight.isOn()).thenReturn(true);                        // Used in header
        when(mockLight.isAutomationEnabled()).thenReturn(false);       // Also in header

        SmartLightActionsMenu.show(mockLight);

        verify(mockLight).turnOff();
    }


    @Test
    void whenToggleAutomationSelected_thenAutomationToggles() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("3", "8");

        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE);
        when(mockLight.isOn()).thenReturn(true);
        when(mockLight.isAutomationEnabled())
                .thenReturn(false, false, false, false); // 🔥 All calls say "disabled"

        SmartLightActionsMenu.show(mockLight);

        verify(mockLight, atLeastOnce()).isAutomationEnabled();
        verify(mockLight).setAutomationEnabled(true); // ✅ Test now matches behavior
    }

    @Test
    void whenSetLightModeSelected_onSupportedDevice_thenAppliesCustomMode() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("5", "8");

        when(mockLight.supportsCustomMode()).thenReturn(true);
        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE); // 🧯 for header
        when(mockLight.getLightMode()).thenReturn(null);               // optional
        when(mockLight.isOn()).thenReturn(true);
        when(mockLight.isAutomationEnabled()).thenReturn(true);

        SmartLightActionsMenu.show(mockLight);

        verify(mockLight).setLightMode(any(SmartLightAction.class));
        verify(mockLight).turnOn();
        verify(mockLight).applyEffect(SmartLightEffect.NONE);
    }

    @Test
    void whenUnsupportedLightModeSelected_thenShowWarning() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("5", "8");

        when(mockLight.supportsCustomMode()).thenReturn(false);
        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE);
        when(mockLight.getLightMode()).thenReturn(null);
        when(mockLight.isOn()).thenReturn(true);
        when(mockLight.isAutomationEnabled()).thenReturn(true);

        SmartLightActionsMenu.show(mockLight);

        assertTrue(outContent.toString().contains("⚠️ This model does not support static light modes."));
    }


    @Test
    void whenInvalidOptionSelected_thenShowErrorMessage() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("99", "8");

        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE);
        when(mockLight.getLightMode()).thenReturn(null);
        when(mockLight.isOn()).thenReturn(true);
        when(mockLight.isAutomationEnabled()).thenReturn(true);

        SmartLightActionsMenu.show(mockLight);

        assertTrue(outContent.toString().contains("❌ Invalid choice."));
    }


    @Test
    void whenExitSelected_thenLoopTerminates() {
        SmartLight mockLight = mock(SmartLight.class);
        feedMockInput("8");

        when(mockLight.getLiteFx()).thenReturn(SmartLightEffect.NONE);
        when(mockLight.getLightMode()).thenReturn(null);
        when(mockLight.isOn()).thenReturn(true);
        when(mockLight.isAutomationEnabled()).thenReturn(true);

        SmartLightActionsMenu.show(mockLight);

        String output = outContent.toString();
        assertTrue(output.contains("Smart Light Actions"));
        assertTrue(output.contains("Back"));
    }
}
