package uiTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import scheduler.Scheduler;
import storage.DeviceStorage;
import ui.DeviceStateManager;
import utils.EmailService;

import static org.mockito.Mockito.*;

class DeviceStateManagerTest {

    @Test
    void whenTurningDeviceOn_thenStateUpdatedAndEmailSent() {
        // Arrange
        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn("D1");
        when(mockDevice.getName()).thenReturn("Fridge");
        when(mockDevice.getType()).thenReturn(DeviceType.THERMOSTAT);

        Scheduler mockScheduler = mock(Scheduler.class);

        try (
                MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class);
                MockedStatic<EmailService> emailService = mockStatic(EmailService.class)
        ) {
            // Act
            DeviceStateManager.updateState(mockDevice, mockScheduler, true);

            // Assert
            verify(mockScheduler).removeTaskIfConflicts("D1", "ON");
            verify(mockDevice).setState("ON");
            deviceStorage.verify(() -> DeviceStorage.updateDeviceState("D1", "ON"));
            emailService.verify(() -> EmailService.sendDeviceActionEmail(
                    "javagoldin@gmail.com",
                    "THERMOSTAT",
                    "D1",
                    "Fridge",
                    "ON"
            ));
        }
    }

    @Test
    void whenTurningDeviceOff_thenStateUpdatedAndEmailSent() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn("D2");
        when(mockDevice.getName()).thenReturn("Washer");
        when(mockDevice.getType()).thenReturn(DeviceType.WASHING_MACHINE);

        Scheduler mockScheduler = mock(Scheduler.class);

        try (
                MockedStatic<DeviceStorage> deviceStorage = mockStatic(DeviceStorage.class);
                MockedStatic<EmailService> emailService = mockStatic(EmailService.class)
        ) {
            DeviceStateManager.updateState(mockDevice, mockScheduler, false);

            verify(mockScheduler).removeTaskIfConflicts("D2", "OFF");
            verify(mockDevice).setState("OFF");
            deviceStorage.verify(() -> DeviceStorage.updateDeviceState("D2", "OFF"));
            emailService.verify(() -> EmailService.sendDeviceActionEmail(
                    "javagoldin@gmail.com",
                    "WASHING_MACHINE",
                    "D2",
                    "Washer",
                    "OFF"
            ));
        }
    }
}
