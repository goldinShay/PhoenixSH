package uiTests;

import devices.Device;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import scheduler.Scheduler;
import sensors.Sensor;
import storage.DeviceStorage;
import storage.SensorStorage;
import ui.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Mockito.*;

class MenuTest {

    private Scanner scannerFrom(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @AfterEach
    void resetDeviceStorage() {
        DeviceStorage.getDevices().clear();
        SensorStorage.getSensors().clear();
    }

    @Test
    void whenUserChoosesExit_thenMenuExitsGracefully() {
        Scanner input = scannerFrom("5\n");
        Menu.show(new HashMap<>(), new ArrayList<>(), mock(Scheduler.class), input);
        // ✅ Success = no exception and returns cleanly
    }

    @Test
    void whenUserSelectsDeviceMenu_thenDeviceMenuIsTriggered() {
        try (MockedStatic<DeviceMenu> menu = mockStatic(DeviceMenu.class)) {
            Scanner input = scannerFrom("1\n5\n"); // call menu, then exit
            Menu.show(new HashMap<>(), new ArrayList<>(), mock(Scheduler.class), input);
            menu.verify(() -> DeviceMenu.DevicesMenu(any(), any()));
        }
    }

    @Test
    void whenUserSelectsMonitor_thenDelegatesToDeviceMonitor() {
        try (MockedStatic<DeviceMonitor> monitor = mockStatic(DeviceMonitor.class)) {
            Scanner input = scannerFrom("2\n5\n"); // select → exit
            Menu.show(new HashMap<>(), new ArrayList<>(), mock(Scheduler.class), input);
            monitor.verify(() -> DeviceMonitor.showMonitorDeviceMenu(any(), any()));
        }
    }

    @Test
    void whenUserSelectsScheduler_thenDelegatesToScheduleMenu() {
        try (MockedStatic<ScheduleMenu> schedule = mockStatic(ScheduleMenu.class)) {
            Scanner input = scannerFrom("3\n5\n");
            Menu.show(new HashMap<>(), new ArrayList<>(), mock(Scheduler.class), input);
            schedule.verify(() -> ScheduleMenu.ScheduleMenu(any(), any()));
        }
    }

    @Test
    void whenUserSelectsTestDevice_thenDeviceTestIsTriggered() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("D1");
        when(device.getName()).thenReturn("Coffee Maker");
        when(device.isOn()).thenReturn(false);
        when(device.getState()).thenReturn("OFF");

        Sensor sensor = mock(Sensor.class);
        when(sensor.getSensorId()).thenReturn("S1");
        when(sensor.getSensorName()).thenReturn("Thermo");
        when(sensor.getCurrentReading()).thenReturn(25.69);
        when(sensor.getUnit()).thenReturn("C");

        Map<String, Device> devices = new HashMap<>();
        devices.put("D1", device);

        Map<String, Sensor> sensors = new HashMap<>();
        sensors.put("S1", sensor);

        try (
                MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class);
                MockedStatic<SensorStorage> sensorStorage = mockStatic(SensorStorage.class)
        ) {
            deviceStorage.when(DeviceStorage::getDevices).thenReturn(devices);
            sensorStorage.when(SensorStorage::getSensors).thenReturn(sensors);

            Scanner input = scannerFrom("4\nD1\n5\n"); // test device, then exit
            Menu.show(devices, new ArrayList<>(), mock(Scheduler.class), input);

            verify(device).testDevice();
        }
    }

    @Test
    void whenDeviceAlreadyOn_thenTestIsNotTriggered() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("D9");
        when(device.getName()).thenReturn("Heater");
        when(device.isOn()).thenReturn(true);
        when(device.getState()).thenReturn("ON");

        Map<String, Device> devices = Map.of("D9", device);

        try (MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class)) {
            deviceStorage.when(DeviceStorage::getDevices).thenReturn(devices);

            Scanner input = scannerFrom("4\nD9\n5\n"); // attempt to test ON device
            Menu.show(devices, new ArrayList<>(), mock(Scheduler.class), input);

            verify(device, never()).testDevice();
        }
    }

    @Test
    void whenSensorIsSelected_thenTestSensorBehaviorIsCalled() {
        Sensor sensor = mock(Sensor.class);
        when(sensor.getSensorId()).thenReturn("SENSE");
        when(sensor.getSensorName()).thenReturn("Hygrometer");
        when(sensor.getCurrentReading()).thenReturn(55.69);
        when(sensor.getUnit()).thenReturn("%");

        Map<String, Sensor> sensors = Map.of("SENSE", sensor);

        try (
                MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class);
                MockedStatic<SensorStorage> sensorStorage = mockStatic(SensorStorage.class)
        ) {
            deviceStorage.when(DeviceStorage::getDevices).thenReturn(Collections.emptyMap());
            sensorStorage.when(SensorStorage::getSensors).thenReturn(sensors);

            Scanner input = scannerFrom("4\nSENSE\n5\n");
            Menu.show(new HashMap<>(), new ArrayList<>(), mock(Scheduler.class), input);

            verify(sensor).testSensorBehavior();
        }
    }
}
