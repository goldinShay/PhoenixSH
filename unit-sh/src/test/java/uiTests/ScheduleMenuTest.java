package uiTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.Test;
import scheduler.Scheduler;
import ui.ScheduleMenu;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class ScheduleMenuTest {

    private Scanner scannerFrom(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whenChoosingViewTasks_thenPrintTasksIsCalled() {
        Scheduler mockScheduler = mock(Scheduler.class);
        Scanner scanner = scannerFrom("1\n5\n");

        ScheduleMenu.ScheduleMenu(new HashMap<>(), mockScheduler, scanner);

        verify(mockScheduler).printScheduledTasks();
    }

    @Test
    void whenChoosingSetTask_thenDeviceIsScheduled() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("D1");
        when(device.getName()).thenReturn("Lamp");
        when(device.getType()).thenReturn(DeviceType.LIGHT);

        Scheduler mockScheduler = mock(Scheduler.class);

        Map<String, Device> devices = Map.of("D1", device);

        // Choice 2 (set task) → ID → action → today → time → repeat → exit
        String input = """
            2
            D1
            on
            1
            12:30
            2
            5
            """;
        Scanner scanner = scannerFrom(input);

        ScheduleMenu.ScheduleMenu(devices, mockScheduler, scanner);

        verify(mockScheduler).scheduleTask(eq(device), eq("on"), any(LocalDateTime.class), eq("daily"));
    }

    @Test
    void whenChoosingUpdateTask_thenUpdateTaskIsCalled() {
        Scheduler mockScheduler = mock(Scheduler.class);
        Scanner scanner = scannerFrom("""
            3
            1
            2025-07-10 15:30
            weekly
            5
            """);

        ScheduleMenu.ScheduleMenu(new HashMap<>(), mockScheduler, scanner);

        verify(mockScheduler).updateTask(eq(0), any(LocalDateTime.class), eq("weekly"));
    }

    @Test
    void whenChoosingDeleteTask_thenRemoveTaskIsCalled() {
        Scheduler mockScheduler = mock(Scheduler.class);
        Scanner scanner = scannerFrom("4\n1\n5\n");

        ScheduleMenu.ScheduleMenu(new HashMap<>(), mockScheduler, scanner);

        verify(mockScheduler).removeTask(0);
    }

    @Test
    void whenInvalidDeviceIdEntered_thenScheduleAbortsGracefully() {
        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn("D1");
        when(mockDevice.getType()).thenReturn(DeviceType.LIGHT);
        when(mockDevice.getName()).thenReturn("Bulb");

        Scheduler mockScheduler = mock(Scheduler.class);

        Map<String, Device> devices = Map.of("D1", mockDevice);
        Scanner scanner = scannerFrom("2\nINVALID\n5\n");

        ScheduleMenu.ScheduleMenu(devices, mockScheduler, scanner);

        verify(mockScheduler, never()).scheduleTask(any(), any(), any(), any());
    }
}
