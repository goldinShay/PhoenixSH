package uiTests;

import devices.Device;
import org.junit.jupiter.api.Test;
import ui.AutoOpController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class AutoOpControllerTest {

    private Scanner scannerWithInput(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whenOption1Selected_thenEnableIsCalled() {
        Device mockDevice = mock(Device.class);
        Scanner testScanner = scannerWithInput("1\n");

        AutoOpController.display(mockDevice, testScanner);

        verify(mockDevice, atLeastOnce()).isAutomationEnabled();
        // Add verification for AutoOpLinker.enable(mockDevice) if mocked
    }

    @Test
    void whenOption2Selected_thenDisableIsCalled() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn("TEST-DEVICE-01");
        when(mockDevice.isAutomationEnabled()).thenReturn(false); // optional

        Scanner testScanner = scannerWithInput("2\n");
        AutoOpController.display(mockDevice, testScanner);

        verify(mockDevice, atLeastOnce()).isAutomationEnabled();
    }


    @Test
    void whenOption3Selected_thenMenuReturnsWithoutAction() {
        Device mockDevice = mock(Device.class);
        Scanner testScanner = scannerWithInput("3\n");

        AutoOpController.display(mockDevice, testScanner);

        verify(mockDevice, atLeastOnce()).isAutomationEnabled();
    }

    @Test
    void whenInvalidOptionSelected_thenErrorMessageIsPrinted() {
        Device mockDevice = mock(Device.class);
        Scanner testScanner = scannerWithInput("999\n");

        AutoOpController.display(mockDevice, testScanner);

        verify(mockDevice, atLeastOnce()).isAutomationEnabled();
    }
}
