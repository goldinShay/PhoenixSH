package uiTests;

import devices.Device;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import sensors.Sensor;
import storage.SensorStorage;
import autoOp.AutoOpUnlinker;

import java.util.List;

import static org.mockito.Mockito.*;

class AutoOpUnlinkerTest {

    @AfterEach
    void cleanup() {
        SensorStorage.getSensors().clear();
    }

    @Test
    void whenDeviceLinkedToSensor_thenUnlinkAndCleanPersistence() {
        // Arrange
        Device device = mock(Device.class);
        when(device.getAutomationSensorId()).thenReturn("S1");
        when(device.getId()).thenReturn("D1");
        when(device.getName()).thenReturn("Living Room Lamp");

        Sensor sensor = mock(Sensor.class);

        @SuppressWarnings("unchecked")
        List<Device> mockSlaves = (List<Device>) mock(List.class);
        when(sensor.getLinkedDevice()).thenReturn(mockSlaves);


        SensorStorage.getSensors().put("S1", sensor);

        // ✅ Reference the proper interface, not XlCreator.DevicePersistence
        storage.DevicePersistence persistence = mock(storage.DevicePersistence.class);
        when(persistence.updateDevice(device)).thenReturn(true);
        when(persistence.removeSensorLink("D1")).thenReturn(true);
        AutoOpUnlinker.setPersistence(persistence);

        // Act
        AutoOpUnlinker.disable(device);

        // Assert
        verify(device).setAutomationEnabled(false);
        verify(device).setAutomationSensorId(null);
        verify(device).setLinkedSensor(null);
        verify(device).disableAutoMode();
        verify(sensor.getLinkedDevice()).remove(device);
        verify(persistence).updateDevice(device);
        verify(persistence).removeSensorLink("D1");
    }


    @Test
    void whenPersistenceUpdateFails_thenNoFurtherUnlinkingHappens() {
        Device device = mock(Device.class);
        when(device.getAutomationSensorId()).thenReturn("S1");

        storage.DevicePersistence persistence = mock(storage.DevicePersistence.class);
        when(persistence.updateDevice(device)).thenReturn(false);
        AutoOpUnlinker.setPersistence(persistence);

        AutoOpUnlinker.disable(device);

        verify(device).setAutomationEnabled(false);
        verify(device).disableAutoMode();
        verify(persistence).updateDevice(device);
        verify(persistence, never()).removeSensorLink(any());
    }

    @Test
    void whenNoSensorFound_thenSkipsSensorUnlinkStep() {
        Device device = mock(Device.class);
        when(device.getAutomationSensorId()).thenReturn("MISSING");
        when(device.getId()).thenReturn("D2");
        when(device.getName()).thenReturn("Air Purifier");

        storage.DevicePersistence persistence = mock(storage.DevicePersistence.class);
        when(persistence.updateDevice(device)).thenReturn(true);
        when(persistence.removeSensorLink("D2")).thenReturn(true);
        AutoOpUnlinker.setPersistence(persistence);

        AutoOpUnlinker.disable(device);

        verify(device).setAutomationEnabled(false);
        verify(device).disableAutoMode();
        verify(persistence).removeSensorLink("D2");
        // No sensor exists → skip sensor.getSlaves().remove()
    }
}
