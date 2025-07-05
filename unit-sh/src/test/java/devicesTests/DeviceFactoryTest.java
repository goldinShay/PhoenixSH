package devicesTests;

import devices.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceFactoryTest {

    private static final Clock testClock = Clock.systemDefaultZone();

    @BeforeEach
    void setUp() {
        // Clear internal registry/map to avoid side effects
        Device.clearDeviceRegistryForTests();
        DeviceFactory.getDevices().clear();
    }

    @Test
    void whenDeviceAdded_getSavedStateShouldReflectItsPowerState() {
        Light light = new Light("L01", "TestLight", testClock, true, 1000.0, 1000.0);
        DeviceFactory.getDevices().put("L01", light);

        assertTrue(DeviceFactory.getSavedState("L01"));
    }

    @Test
    void whenDeviceNotFound_getSavedStateShouldReturnFalse() {
        assertFalse(DeviceFactory.getSavedState("UnknownID"));
    }

    @Test
    void whenValidTypeNameProvided_createDeviceByTypeShouldReturnDevice() {
        Device device = DeviceFactory.createDeviceByType(
                "LIGHT", "L02", "MyLight", testClock, Map.of()
        );

        assertTrue(device instanceof Light);
        assertEquals("MyLight", device.getName());
    }

    @Test
    void whenInvalidTypeNameProvided_createDeviceByTypeShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DeviceFactory.createDeviceByType("TOASTER", "X01", "FailingDevice", testClock, Map.of());
        });

        assertTrue(exception.getMessage().contains("Invalid or unsupported device type"));
    }

    @Test
    void whenRequestingDeviceMap_shouldReturnReferenceToInternalMap() {
        Map<String, Device> map = DeviceFactory.getDevices();
        assertNotNull(map);

        map.put("TEST123", new Light("TEST123", "RefTest", testClock, false, 1000.0, 1000.0));

        assertTrue(DeviceFactory.getDevices().containsKey("TEST123"));
    }
}
