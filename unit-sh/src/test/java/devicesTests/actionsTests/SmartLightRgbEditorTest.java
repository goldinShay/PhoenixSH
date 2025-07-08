package devicesTests.actionsTests;

import devices.SmartLight;
import devices.actions.SmartLightAction;
import devices.actions.SmartLightEffect;
import devices.actions.SmartLightRgbEditor;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import utils.Input;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmartLightRgbEditorTest {

    SmartLight mockLight;

    @BeforeEach
    void setup() {
        mockLight = mock(SmartLight.class);
    }

    @Test
    void whenNoCurrentLightMode_thenAppliesDefaultAndTurnsOn() {
        when(mockLight.getLightMode()).thenReturn(null);

        try (MockedStatic<Input> mockedInput = mockStatic(Input.class)) {
            mockedInput.when(() -> Input.getInt(anyString())).thenReturn(7); // immediately choose "Back"

            SmartLightRgbEditor.launchRgbEditor(mockLight);

            verify(mockLight).setLightMode(any(SmartLightAction.class));
            verify(mockLight).turnOn();
            verify(mockLight).applyEffect(SmartLightEffect.NONE);
        }
    }

    @Test
    void whenUserAdjustsRed_thenRedValueIsUpdatedInCustomMode() {
        SmartLightAction initial = new SmartLightAction("CUSTOM", 80, 50, 50, 50);
        when(mockLight.getLightMode()).thenReturn(initial);

        try (MockedStatic<Input> mockedInput = mockStatic(Input.class)) {
            mockedInput.when(() -> Input.getInt("Choice: ")).thenReturn(1, 6, 7); // Set Red, Apply, Back
            mockedInput.when(() -> Input.getInt("Enter Red value (0-100): ", 0, 100)).thenReturn(90);

            SmartLightRgbEditor.launchRgbEditor(mockLight);

            verify(mockLight).setLightMode(argThat(action ->
                    action.getRed() == 90 &&
                            action.getGreen() == 50 &&
                            action.getBlue() == 50 &&
                            action.getIntensity() == 80
            ));
            verify(mockLight).turnOn();
        }
    }

    @Test
    void whenUserIncreasesIntensity_thenNewIntensityIsClampedAt100() {
        SmartLightAction mode = new SmartLightAction("CUSTOM", 95, 100, 100, 100);
        when(mockLight.getLightMode()).thenReturn(mode);

        try (MockedStatic<Input> mockedInput = mockStatic(Input.class)) {
            mockedInput.when(() -> Input.getInt("Choice: ")).thenReturn(4, 6, 7); // +Intensity, Apply, Back

            SmartLightRgbEditor.launchRgbEditor(mockLight);

            verify(mockLight).setLightMode(argThat(a -> a.getIntensity() == 100));
        }
    }

    @Test
    void whenUserChoosesBackImmediately_thenNoChangesAreApplied() {
        when(mockLight.getLightMode()).thenReturn(new SmartLightAction("CUSTOM", 60, 30, 30, 30));

        try (MockedStatic<Input> mockedInput = mockStatic(Input.class)) {
            mockedInput.when(() -> Input.getInt("Choice: ")).thenReturn(7); // Back
            SmartLightRgbEditor.launchRgbEditor(mockLight);
            verify(mockLight, never()).setLightMode(any());
        }
    }
}
