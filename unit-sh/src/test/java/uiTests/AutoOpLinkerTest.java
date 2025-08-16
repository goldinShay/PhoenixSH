package uiTests;

import devices.Device;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sensors.Sensor;
import storage.SensorStorage;
import autoOp.AutoOpLinker;
import autoOp.AutoOpManager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class AutoOpLinkerTest {

    private Scanner scannerFrom(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @AfterEach
    void cleanup() {
        SensorStorage.getSensors().clear();
    }

    @Test
    void whenSensorSelected_thenDeviceIsLinkedAndEnabled() {
        // Arrange
        Device mockDevice = mock(Device.class);
        when(mockDevice.getName()).thenReturn("MyDevice");

        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getSensorId()).thenReturn("S1");
        when(mockSensor.getSensorName()).thenReturn("TempSensor");
        when(mockSensor.getCurrentReading()).thenReturn(23.0);

        SensorStorage.getSensors().put("S1", mockSensor);

        try (MockedStatic<AutoOpManager> mockAutoOp = mockStatic(AutoOpManager.class)) {
            mockAutoOp.when(() -> AutoOpManager.persistLink(mockDevice, mockSensor)).thenReturn(true);

            // Act
            AutoOpLinker.enable(mockDevice, scannerFrom("S1\n"));

            // Assert
            verify(mockSensor).addSlave(mockDevice);
            verify(mockDevice).setAutomationSensorId("S1");
            verify(mockDevice).setAutomationEnabled(true);
            verify(mockDevice).enableAutoMode();
        }
    }

    @Test
    void whenSensorListIsEmpty_thenNothingIsLinked() {
        Device mockDevice = mock(Device.class);

        AutoOpLinker.enable(mockDevice, scannerFrom("anything\n"));

        verify(mockDevice, never()).enableAutoMode();
    }

    @Test
    void whenSensorIdNotFound_thenDeviceIsNotLinked() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getName()).thenReturn("Lamp");

        Sensor mockSensor = mock(Sensor.class);
        when(mockSensor.getSensorId()).thenReturn("REAL");
        when(mockSensor.getSensorName()).thenReturn("Sensor");
        when(mockSensor.getCurrentReading()).thenReturn(42.69);

        SensorStorage.getSensors().put("REAL", mockSensor);

        AutoOpLinker.enable(mockDevice, scannerFrom("WRONG\n"));

        verify(mockDevice, never()).enableAutoMode();
        verify(mockDevice, never()).setAutomationEnabled(anyBoolean());
    }

    @Test
    void whenSensorThrowsReadingException_thenDisplayStillWorks() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getName()).thenReturn("AirPurifier");

        Sensor flakySensor = mock(Sensor.class);
        when(flakySensor.getSensorId()).thenReturn("ERR1");
        when(flakySensor.getSensorName()).thenReturn("HumiditySensor");
        when(flakySensor.getCurrentReading()).thenThrow(new RuntimeException("Data timeout"));

        SensorStorage.getSensors().put("ERR1", flakySensor);

        AutoOpLinker.enable(mockDevice, scannerFrom("INVALID\n"));

        verify(flakySensor).getCurrentReading();
        verify(mockDevice, never()).enableAutoMode();
    }
}
