package uiTests.deviceActionMenuTests;

import devices.Dryer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.AutoOpController;
import ui.deviceActionMenu.DryerActionsMenu;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class DryerActionsMenuTest {

    private Scanner scannerFrom(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whenDeviceIsNotDryer_thenWarnsAndReturns() {
        DryerActionsMenu.show(mock(devices.Device.class)); // Should NOT throw, just return
    }

    @Test
    void boschDryer_activatesEcoAndRapidAndStatus() {
        Dryer bosch = mock(Dryer.class);
        when(bosch.getModel()).thenReturn("BDR14025");
        when(bosch.isOn()).thenReturn(false);
        when(bosch.isRunning()).thenReturn(false);
        when(bosch.isAutomationEnabled()).thenReturn(false);

        String input = """
            1
            2
            3
            4
            5
            6
            7
            8
            9
            10
            """;

        try (MockedStatic<AutoOpController> autoOp = mockStatic(AutoOpController.class)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            DryerActionsMenu.show(bosch);

            verify(bosch).turnOn();
            verify(bosch).turnOff();
            verify(bosch).start();
            verify(bosch).stop();
            autoOp.verify(() -> AutoOpController.display(bosch));
            verify(bosch).setMode("EcoDry");
            verify(bosch).setMode("RapidDry");
            verify(bosch).setMode("AntiCrease");
            verify(bosch, times(1)).status(); // Only one call in case "9"
        }
    }
    private final InputStream originalIn = System.in;
    @AfterEach
    void restoreSystemIn() {
        System.setIn(originalIn); // 🌱 Fully restores standard input
    }


    @Test
    void nonFlagshipDryer_showsUnavailableAndReturns() {
        Dryer regular = mock(Dryer.class);
        when(regular.getModel()).thenReturn("ANYTHING_ELSE");
        when(regular.isOn()).thenReturn(false);
        when(regular.isRunning()).thenReturn(false);
        when(regular.isAutomationEnabled()).thenReturn(false);

        String input = """
            1
            2
            3
            4
            5
            6
            7
            8
            """;

        try (MockedStatic<AutoOpController> autoOp = mockStatic(AutoOpController.class)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            DryerActionsMenu.show(regular);

            verify(regular).turnOn();
            verify(regular).turnOff();
            verify(regular).start();
            verify(regular).stop();
            autoOp.verify(() -> AutoOpController.display(regular));
            verify(regular).status(); // Triggered by case "7"
        }
    }
}

