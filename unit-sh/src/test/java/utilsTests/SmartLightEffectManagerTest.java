package utilsTests;

import devices.SmartLight;
import devices.actions.SmartLightEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import utils.Input;
import utils.SmartLightEffectManager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class SmartLightEffectManagerTest {

    @AfterEach
    void restoreDefaultScanner() {
        Input.setScanner(new Scanner(System.in));
    }

    @Test
    void whenNoEffectIsActive_thenUserCanSelectAndApplyEffect() {
        String simulatedInput = "2\n"; // Simulate choosing second effect
        Input.setScanner(new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8))));

        SmartLight smartMock = mock(SmartLight.class);
        when(smartMock.getLiteFx()).thenReturn(SmartLightEffect.NONE);

        SmartLightEffectManager.handleEffectToggle(smartMock);

        verify(smartMock).applyEffect(SmartLightEffect.values()[1]); // Index 2 → values()[1]
    }

    @Test
    void whenEffectIsActive_thenToggleDisablesIt() {
        SmartLight smartMock = mock(SmartLight.class);
        when(smartMock.getLiteFx()).thenReturn(SmartLightEffect.FIRE);

        SmartLightEffectManager.handleEffectToggle(smartMock);

        verify(smartMock).applyEffect(SmartLightEffect.NONE);
    }
}
