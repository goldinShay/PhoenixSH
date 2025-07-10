package schedulerTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduler.Scheduler;
import scheduler.ScheduledTask;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {

    private static class MockDevice extends Device {
        private boolean actionPerformed = false;
        private String lastAction = "";

        public MockDevice(String id, String name) {
            super(id, name, DeviceType.UNKNOWN, Clock.systemDefaultZone(), 0, 0);
        }

        @Override
        public List<String> getAvailableActions() {
            return List.of("on", "off");
        }

        @Override
        public void simulate(String action) {
            this.lastAction = action;
            this.actionPerformed = true;
        }

        public boolean wasActionPerformed() {
            return actionPerformed;
        }

        public String getLastAction() {
            return lastAction;
        }
    }

    private Scheduler scheduler;
    private MockDevice device;

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();
        device = new MockDevice("DEV001", "TestDevice");
        Map<String, Device> devices = new HashMap<>();
        devices.put(device.getId(), device);
        scheduler = new Scheduler(devices, new HashMap<>());
    }

    @Test
    void scheduleTask_shouldAddTaskToList() {
        int initialSize = getScheduledTasks().size();
        scheduler.scheduleTask(device, "ON", LocalDateTime.now().plusMinutes(1), "daily");
        assertEquals(initialSize + 1, getScheduledTasks().size());
    }

    @Test
    void removeTask_shouldDeleteCorrectTask() {
        scheduler.scheduleTask(device, "ON", LocalDateTime.now().plusMinutes(1), "daily");
        scheduler.removeTask(0);
        assertEquals(0, getScheduledTasks().size());
    }

    @Test
    void updateTask_shouldChangeTimeAndRepeat() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);
        scheduler.scheduleTask(device, "ON", LocalDateTime.now(), "none");
        scheduler.updateTask(0, newTime, "weekly");
        ScheduledTask updated = getScheduledTasks().get(0);
        assertEquals("weekly", updated.getRepeat());
        assertEquals(newTime, updated.getTime());
    }

    @Test
    void removeTaskIfConflicts_shouldRemoveOppositeAction() {
        scheduler.scheduleTask(device, "OFF", LocalDateTime.now(), "none");
        scheduler.removeTaskIfConflicts("DEV001", "ON");
        assertEquals(0, getScheduledTasks().size());
    }

    @Test
    void checkAndRunDueTasks_shouldTriggerDeviceAction() {
        scheduler.scheduleTask(device, "OFF", LocalDateTime.now().minusSeconds(10), "none");
        invokeDueTaskCheck();
        assertTrue(device.wasActionPerformed());
        assertEquals("OFF", device.getLastAction());
    }

    // Helper to access private scheduler list (pretend it's package-private or expose for test)
    @SuppressWarnings("unchecked")
    private List<ScheduledTask> getScheduledTasks() {
        try {
            var field = Scheduler.class.getDeclaredField("scheduledTasks");
            field.setAccessible(true);
            return (List<ScheduledTask>) field.get(scheduler);
        } catch (Exception e) {
            throw new RuntimeException("Unable to access scheduledTasks", e);
        }
    }

    private void invokeDueTaskCheck() {
        try {
            var method = Scheduler.class.getDeclaredMethod("checkAndRunDueTasks");
            method.setAccessible(true);
            method.invoke(scheduler);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke checkAndRunDueTasks", e);
        }
    }
}
