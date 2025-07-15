package storageTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.DeviceStorage;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceStorageTest {

    private static class TestDevice extends Device {
        public boolean turnedOn = false;
        public boolean turnedOff = false;

        public TestDevice(String id) {
            super(id, "StubDevice", DeviceType.UNKNOWN, Clock.systemDefaultZone(), 1000.0, 1000.0);
        }

        @Override
        public List<String> getAvailableActions() {
            return List.of("on", "off");
        }

        @Override
        public void simulate(String action) {}

        @Override
        public void turnOn() {
            super.turnOn();
            turnedOn = true;
        }

        @Override
        public void turnOff() {
            super.turnOff();
            turnedOff = true;
        }
    }

    private TestDevice device;

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();
        DeviceStorage.getDevices().clear();
        device = new TestDevice("D001");
        DeviceStorage.getDevices().put(device.getId(), device);
    }

    @Test
    void getDeviceList_shouldReturnAllDevicesAsList() {
        List<Device> list = DeviceStorage.getDeviceList();
        assertEquals(1, list.size());
        assertEquals("D001", list.get(0).getId());
    }

    @Test
    void updateDeviceState_on_shouldTurnDeviceOn() {
        DeviceStorage.updateDeviceState("D001", "ON");
        assertTrue(device.turnedOn);
        assertTrue(device.isOn());
    }

    @Test
    void updateDeviceState_off_shouldTurnDeviceOff() {
        device.turnOn(); // Set up ON state
        DeviceStorage.updateDeviceState("D001", "OFF");
        assertTrue(device.turnedOff);
        assertFalse(device.isOn());
    }

    @Test
    void updateDeviceState_invalidId_shouldDoNothing() {
        DeviceStorage.updateDeviceState("UNKNOWN", "ON");
        // No crash, no effect expected
        assertFalse(device.turnedOn);
    }

    @Test
    void getDeviceThreads_shouldReturnStaticList() {
        List<Thread> threads = DeviceStorage.getDeviceThreads();
        assertNotNull(threads);
        assertSame(threads, DeviceStorage.getDeviceThreads());
    }
}
