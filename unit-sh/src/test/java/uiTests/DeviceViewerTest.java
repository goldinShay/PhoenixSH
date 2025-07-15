package uiTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import ui.DeviceViewer;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceViewerTest {

    @AfterEach
    void resetDisplayHook() {
        DeviceViewer.resetDisplayHook();
    }

    @Test
    void getAllDevicesAndSensors_returnsMergedDeviceAndSensorMap() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn("D1");

        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getSensorId()).thenReturn("S1");

        Map<String, Device> devices = new LinkedHashMap<>();
        devices.put("D1", mockDevice);

        Map<String, Sensor> sensors = new LinkedHashMap<>();
        sensors.put("S1", mockSensor);

        try (
                MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class);
                MockedStatic<SensorStorage> sensorStorage = mockStatic(SensorStorage.class)
        ) {
            deviceStorage.when(DeviceStorage::getDevices).thenReturn(devices);
            sensorStorage.when(SensorStorage::getUnmodifiableSensors).thenReturn(sensors);

            Map<String, Object> result = DeviceViewer.getAllDevicesAndSensors();

            assertEquals(2, result.size());
            assertTrue(result.containsKey("D1"));
            assertTrue(result.containsKey("S1"));
            assertSame(mockDevice, result.get("D1"));
            assertSame(mockSensor, result.get("S1"));
        }
    }

    @Test
    void displayAllDevicesAndSensors_runsCustomDisplayHook() {
        Runnable mockHook = mock(Runnable.class);
        DeviceViewer.setDisplayHook(mockHook);

        DeviceViewer.displayAllDevicesAndSensors();

        verify(mockHook).run();
    }

    @Test
    void setDisplayHook_nullFallbacksToDefault() {
        DeviceViewer.setDisplayHook(null); // should fallback
        assertDoesNotThrow(DeviceViewer::displayAllDevicesAndSensors);
    }
}
