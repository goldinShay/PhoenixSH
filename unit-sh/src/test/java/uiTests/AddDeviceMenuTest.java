package uiTests;

import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import org.junit.jupiter.api.*;
import storage.DeviceStorage;
import ui.AddDeviceMenu;

import java.io.*;
import java.time.Clock;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AddDeviceMenuTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    private Map<String, Device> testDevices;
    private List<Thread> testDeviceThreads;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        testDevices = new HashMap<>();
        testDeviceThreads = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        DeviceStorage.clear(); // 🧼 Make sure storage is reset for other tests too
    }

    @Test
    void addDeviceMenu_shouldAddDevice_whenValidInputProvided() {
        // 🧪 Simulate choosing first device type and giving a valid name
        String simulatedInput = "1\nMyLight\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Reset global state and override factory to inject a dynamic test device
        DeviceStorage.clear();

        DeviceFactory.setDeviceCreator((id, name, clock, map) ->
                new FakeDevice(id, name, DeviceType.LIGHT)
        );

        AddDeviceMenu.clock = Clock.systemUTC(); // Safe deterministic clock
        AddDeviceMenu.addDeviceMenu(testDevices, testDeviceThreads);

        // ✅ Assert that a device was added with expected properties
        assertEquals(1, testDevices.size(), "Device should be added");

        String addedId = testDevices.keySet().iterator().next();
        Device added = testDevices.get(addedId);

        assertNotNull(added);
        assertEquals("MyLight", added.getName());
        assertEquals(DeviceType.LIGHT, added.getType());
        assertTrue(outContent.toString().contains("✅ MyLight (" + addedId + ") added successfully"));
    }

    // 🧪 Minimal stub device for safe test injection
    static class FakeDevice extends Device implements Runnable {
        public FakeDevice(String id, String name, DeviceType type) {
            super(id, name, type, Clock.systemUTC(), 400.0, 600.0);
        }

        @Override public void run() {}
        @Override public List<String> getAvailableActions() { return List.of("on", "off"); }
        @Override public void simulate(String action) {}
    }
}
