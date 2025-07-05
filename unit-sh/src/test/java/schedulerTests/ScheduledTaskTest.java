package schedulerTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduler.ScheduledTask;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduledTaskTest {

    private static class MockDevice extends Device {
        public MockDevice(String id, String name, Clock clock) {
            super(id, name, DeviceType.GENERIC, clock, 0.0, 0.0);
        }

        @Override
        public List<String> getAvailableActions() {
            return List.of("mock");
        }

        @Override
        public void simulate(String action) {
        }
    }

    private ScheduledTask task;
    private MockDevice mockDevice;
    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 7, 4, 15, 30);

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests(); // 🧼 Clears REGISTERED_IDS

        mockDevice = new MockDevice("D001", "MockSensor", Clock.systemDefaultZone());
        task = new ScheduledTask(mockDevice, "ON", fixedTime, "Weekly");
    }


    @Test
    void whenCreatedWithRepeat_uppercaseRepeatShouldBeNormalized() {
        assertEquals("weekly", task.getRepeat());
    }

    @Test
    void whenToFileStringCalled_shouldReturnPipeDelimitedRecord() {
        String expected = "D001|MockSensor|ON|2025-07-04-15:30|weekly";
        assertEquals(expected, task.toFileString());
    }

    @Test
    void whenToStringCalled_shouldFormatHumanReadableSummary() {
        String expected = "[04/07/2025 15:30] MockSensor - ON (Repeat: weekly)";
        assertEquals(expected, task.toString());
    }

    @Test
    void equalsShouldReturnTrueForIdenticalTasks() {
        ScheduledTask same = new ScheduledTask(mockDevice, "ON", fixedTime, "WEEKLY");
        assertEquals(task, same);
    }

    @Test
    void equalsShouldReturnFalseForDifferentDevices() {
        Device otherDevice = new MockDevice("D002", "Other", Clock.systemDefaultZone());
        ScheduledTask different = new ScheduledTask(otherDevice, "ON", fixedTime, "weekly");
        assertNotEquals(task, different);
    }

    @Test
    void hashCodeShouldBeConsistentWithEquals() {
        ScheduledTask duplicate = new ScheduledTask(mockDevice, "ON", fixedTime, "weekly");
        assertEquals(task.hashCode(), duplicate.hashCode());
    }
}
