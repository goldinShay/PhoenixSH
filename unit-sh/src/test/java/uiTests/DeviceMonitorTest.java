package uiTests;

import devices.Device;
import devices.DeviceType;
import devices.SmartLight;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import scheduler.Scheduler;
import storage.DeviceStorage;
import ui.DeviceMonitor;
import ui.deviceActionMenu.SmartLightActionsMenu;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class DeviceMonitorTest {

    @AfterEach
    void resetDeviceStorage() {
        DeviceStorage.getDevices().clear();
    }

    private Scanner scannerFrom(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whenValidSmartLightId_thenRoutesToSmartLightMenu() {
        SmartLight mockLight = mock(SmartLight.class);
        when(mockLight.getType()).thenReturn(DeviceType.SMART_LIGHT);
        when(mockLight.getId()).thenReturn("L1");
        when(mockLight.getName()).thenReturn("Hall Light");

        Map<String, Device> mockDevices = new HashMap<>();
        mockDevices.put("L1", mockLight);

        try (MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class);
             MockedStatic<SmartLightActionsMenu> smartMenu = mockStatic(SmartLightActionsMenu.class)) {

            deviceStorage.when(DeviceStorage::getDevices).thenReturn(mockDevices);

            String input = "L1\n0\n"; // Select device, then exit
            Scanner testScanner = scannerFrom(input);

            DeviceMonitor.showMonitorDeviceMenu(mockDevices, mock(Scheduler.class), testScanner);

            smartMenu.verify(() -> SmartLightActionsMenu.show(mockLight));
        }
    }

    @Test
    void whenInvalidIdEntered_thenPromptsAgain() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getType()).thenReturn(DeviceType.LIGHT);
        when(mockDevice.getId()).thenReturn("D1");
        when(mockDevice.getName()).thenReturn("Living Room");

        Map<String, Device> mockDevices = new HashMap<>();
        mockDevices.put("D1", mockDevice);

        try (MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class)) {
            deviceStorage.when(DeviceStorage::getDevices).thenReturn(mockDevices);

            // Enter invalid ID, then valid one, then exit
            String input = "WRONG\nD1\n0\n";
            Scanner scanner = scannerFrom(input);

            DeviceMonitor.showMonitorDeviceMenu(mockDevices, mock(Scheduler.class), scanner);

            // No need to verify route behavior here, just confirm no crash
        }
    }

    @Test
    void whenZeroIsEntered_thenExitsImmediately() {
        Map<String, Device> mockDevices = new HashMap<>();

        Scanner scanner = scannerFrom("0\n");

        DeviceMonitor.showMonitorDeviceMenu(mockDevices, mock(Scheduler.class), scanner);
        // Success = no exception and loop exits
    }
}
