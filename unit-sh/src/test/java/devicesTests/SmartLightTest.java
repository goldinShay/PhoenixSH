package devicesTests;

import devices.DeviceFactory;
import devices.DeviceType;
import devices.SmartLight;
import devices.actions.SmartLightAction;
import devices.actions.SmartLightEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SmartLightTest {

    @BeforeEach
    void clearDeviceFactoryState() {
        DeviceFactory.getDevices().clear();
    }

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2024-01-01T12:00:00Z"),
            ZoneId.systemDefault()
    );

    private SmartLight newSupportedLight() {
        return new SmartLight(uuid(), "Calex Light", "Calex", "Calex A60E27", FIXED_CLOCK, true);
    }

    private SmartLight newUnsupportedLight() {
        return new SmartLight(uuid(), "Generic Light", "Generic", "UnknownModel", FIXED_CLOCK, false);
    }

    private static String uuid() {
        return "TEST-" + UUID.randomUUID().toString();
    }

    @Test
    void whenCreatedWithSupportedModel_thenLightModeIsInitialized() {
        SmartLight light = newSupportedLight();
        assertNotNull(light.getLightMode());
        assertEquals("Calex A60E27", light.getModel());
        assertTrue(light.supportsCustomMode());
    }

    @Test
    void whenCreatedWithUnsupportedModel_thenLightModeIsNull() {
        SmartLight light = newUnsupportedLight();
        assertNull(light.getLightMode());
        assertEquals("UnknownModel", light.getModel());
        assertFalse(light.supportsCustomMode());
    }

    @Test
    void whenApplyingColor_thenRgbValuesAreAccepted() {
        SmartLight light = newSupportedLight();
        light.applyColor(10, 20, 30);
    }

    @Test
    void whenTurningOn_thenDeviceIsMarkedOn() {
        SmartLight light = newUnsupportedLight();
        light.turnOn();
        assertTrue(light.isOn());
    }

    @Test
    void whenTurningOff_thenDeviceIsMarkedOff() {
        SmartLight light = newSupportedLight();
        light.turnOff();
        assertFalse(light.isOn());
    }

    @Test
    void whenGetAvailableActions_thenIncludesSetModeForSupportedModel() {
        SmartLight supported = newSupportedLight();
        SmartLight unsupported = newUnsupportedLight();

        assertTrue(supported.getAvailableActions().contains("setMode"));
        assertFalse(unsupported.getAvailableActions().contains("setMode"));
    }

    @Test
    void whenSettingLightModeForUnsupportedModel_thenModeIsNotSet() {
        SmartLight light = newUnsupportedLight();
        SmartLightAction custom = new SmartLightAction("TEST", 50, 100, 100, 100);
        light.setLightMode(custom);
        assertNull(light.getLightMode());
    }

    @Test
    void whenSettingLightModeForSupportedModel_thenModeIsUpdated() {
        SmartLight light = newSupportedLight();
        SmartLightAction custom = new SmartLightAction("READ", 60, 100, 90, 80);
        light.setLightMode(custom);
        assertEquals(custom.getLabel(), light.getLightMode().getLabel());
    }

    @Test
    void whenApplyingNullEffect_thenEffectDefaultsToNone() {
        SmartLight light = newSupportedLight();
        light.applyEffect(null);
        assertEquals(SmartLightEffect.NONE, light.getEffect());
    }

    @Test
    void whenApplyingEffectToUnsupportedModel_thenEffectRemainsNone() {
        SmartLight light = newUnsupportedLight();
        light.applyEffect(SmartLightEffect.FIRE);
        assertEquals(SmartLightEffect.NONE, light.getEffect());
    }

    @Test
    void whenCallingToDataString_thenReturnsCorrectFormat() {
        SmartLight light = new SmartLight("SL_TEST", "Calex Light", "Calex", "Calex A60E27", FIXED_CLOCK, true);
        String data = light.toDataString();
        assertEquals("SMART_LIGHT|SL_TEST|Calex Light|Calex|Calex A60E27", data);
    }

    @Test
    void whenParsingFromValidData_thenSmartLightIsConstructed() {
        String[] parts = {"SMART_LIGHT", "SL009", "Desk Lamp", "Calex", "Calex A60E27"};
        SmartLight recreated = SmartLight.fromDataString(parts, FIXED_CLOCK);
        assertEquals("Desk Lamp", recreated.getName());
        assertEquals("Calex", recreated.getBrand());
        assertEquals(DeviceType.SMART_LIGHT, recreated.getType());
        assertFalse(recreated.isOn());
    }

    @Test
    void whenParsingInvalidData_thenThrowsException() {
        String[] badParts = {"SMART_LIGHT", "OnlyID"};
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> SmartLight.fromDataString(badParts, FIXED_CLOCK));
        assertTrue(ex.getMessage().startsWith("Invalid data string"));
    }

    @Test
    void whenToStringIsCalled_thenReturnsCorrectRepresentation() {
        SmartLight light = newSupportedLight();
        String result = light.toString();
        assertTrue(result.contains("SmartLight{name="));
        assertTrue(result.contains("Calex A60E27"));
    }
}
