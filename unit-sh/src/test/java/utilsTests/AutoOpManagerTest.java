package utilsTests;

import devices.Device;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import sensors.Sensor;
import autoOp.AutoOpExcelReader;
import autoOp.AutoOpExcelReader.AutoOpRecord;
import storage.DeviceStorage;
import storage.SensorStorage;
import autoOp.AutoOpManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoOpManagerTest {

    Device slaveMock;
    Sensor sensorMock;

    @BeforeEach
    void setUp() {
        slaveMock = mock(Device.class);
        sensorMock = mock(Sensor.class);

        when(slaveMock.getId()).thenReturn("D123");
        when(sensorMock.getSensorId()).thenReturn("S789");
        when(sensorMock.getLinkedDevice()).thenReturn(new ArrayList<>());

        DeviceStorage.getDevices().put("D123", slaveMock);
        SensorStorage.getSensors().put("S789", sensorMock);
    }

    @AfterEach
    void tearDown() {
        DeviceStorage.getDevices().clear();
        SensorStorage.getSensors().clear();
    }

    @Test
    void whenLoadingMappingsFromExcel_thenMemoryLinkIsRestored() {
        AutoOpRecord fakeRecord = new AutoOpRecord("D123", "S789", 10.5, 5.5);
        List<AutoOpRecord> mockRecords = List.of(fakeRecord);

        try (MockedStatic<AutoOpExcelReader> readerMock = mockStatic(AutoOpExcelReader.class)) {
            readerMock.when(AutoOpExcelReader::readLinks).thenReturn(mockRecords);

            AutoOpManager.loadMappingsFromExcel();

            verify(slaveMock).setAutoOnThreshold(10.5, true);
            verify(slaveMock).setAutoOffThreshold(5.5);
            verify(slaveMock).setAutomationEnabled(true);
            verify(slaveMock).setAutomationSensorId("S789");
            verify(sensorMock).addSlave(slaveMock);
        }
    }

    @Test
    void whenUnlinkingDevice_thenReturnsTrueOnSuccess() {
        when(slaveMock.getId()).thenReturn("D123");

        try (var staticMock = mockStatic(storage.XlCreator.class)) {
            staticMock.when(() -> storage.XlCreator.removeFromSenseControl("D123")).thenReturn(true);
            boolean success = AutoOpManager.unlink(slaveMock);
            assertTrue(success);
        }
    }

    @Test
    void whenPersistingLink_thenReturnsTrueOnSuccess() {
        try (var staticMock = mockStatic(storage.XlCreator.class)) {
            staticMock.when(() -> storage.XlCreator.appendToSenseControl(slaveMock, sensorMock)).thenReturn(true);
            boolean success = AutoOpManager.persistLink(slaveMock, sensorMock);
            assertTrue(success);
        }
    }

    @Test
    void whenReevaluatingSensors_thenNotifySlavesIsCalled() {
        SensorStorage.getSensors().put("S789", sensorMock);
        List<Device> slaveList = new ArrayList<>();
        slaveList.add(slaveMock);

        when(sensorMock.getCurrentReading()).thenReturn(42.0);
        when(sensorMock.getLinkedDevice()).thenReturn(slaveList);
        when(slaveMock.getId()).thenReturn("D123");

        AutoOpManager.reevaluateAllSensors();

        verify(sensorMock).notifyLinkedDevices(42.0);
    }
}
