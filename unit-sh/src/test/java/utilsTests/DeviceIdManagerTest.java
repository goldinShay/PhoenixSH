package utilsTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.*;
import utils.DeviceIdManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceIdManagerTest {

    DeviceIdManager manager;

    @BeforeEach
    void setup() {
        manager = DeviceIdManager.getInstance();
        manager.setExistingDevices(List.of()); // 🧼 Clear assigned IDs
    }

    @Test
    void whenNoDevicesExist_thenGenerateIdStartsAt001() {
        String id = manager.generateId("LIGHT");
        assertEquals("LI001", id);
        assertTrue(manager.isIdTaken(id));
    }

    @Test
    void whenDeviceExists_thenGenerateIdIncrementsCorrectly() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn("LI004");

        manager.setExistingDevices(List.of(mockDevice));
        String id = manager.generateId("LIGHT");
        assertEquals("LI005", id);
    }

    @Test
    void whenTypeIsSmartLight_thenPrefixShouldBeSL() {
        String id = manager.generateId("SMART_LIGHT");
        assertTrue(id.startsWith("SL"));
        assertEquals("SL001", id);
    }

    @Test
    void whenCallingGenerateIdForEnum_thenShortcutWorks() {
        String id = manager.generateIdForType(DeviceType.THERMOSTAT);
        assertEquals("TH001", id);
    }

    @Test
    void whenInvalidSuffixesExist_thenOnlyNumericOnesAreUsed() {
        Device d1 = mock(Device.class);
        Device d2 = mock(Device.class);
        Device d3 = mock(Device.class);
        when(d1.getId()).thenReturn("LI001");
        when(d2.getId()).thenReturn("LIxyz"); // ❌ Not numeric
        when(d3.getId()).thenReturn("LI007");

        manager.setExistingDevices(List.of(d1, d2, d3));
        String id = manager.generateId("LIGHT");
        assertEquals("LI008", id);
    }

    @Test
    void whenCheckingIdTaken_thenReturnsCorrectStatus() {
        String id = manager.generateId("DRYER");
        assertTrue(manager.isIdTaken(id));
        assertFalse(manager.isIdTaken("WM999"));
    }
}
