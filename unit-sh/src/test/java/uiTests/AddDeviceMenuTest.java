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
    }

    @Test
    void addDeviceMenu_shouldAddDevice_whenValidInputProvided() {
        // ✅ Simulate choosing first device type and giving a valid name
        String simulatedInput = "1\nMyLight\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Stub DeviceFactory to create a fake device without full logic
        DeviceStorage.getDevices().clear(); // reset global state

        // Inject a fake device
        Device fakeDevice = new FakeDevice("4 errors to this class:LGHT001", "MyLight", DeviceType.LIGHT);
        DeviceFactory.setDeviceCreator((id, name, clock, map) -> fakeDevice);


        AddDeviceMenu.clock = Clock.systemUTC(); // safe clock
        AddDeviceMenu.addDeviceMenu(testDevices, testDeviceThreads);

        assertTrue(testDevices.containsKey("LGHT001"), "Device map should contain the new device");
        assertEquals("MyLight", testDevices.get("LGHT001").getName());
        assertTrue(outContent.toString().contains("✅ MyLight (LGHT001) added successfully"));
    }

    // ✅ Simple stub for safe test
    static class FakeDevice extends Device implements Runnable {
        public FakeDevice(String id, String name, DeviceType type) {
            super(id, name, type, Clock.systemUTC(), 400.0, 600.0); // 👈 Added thresholds
        }


        @Override public void run() {}
        @Override public List<String> getAvailableActions() { return List.of("on", "off"); }
        @Override public void simulate(String action) {}
    }
}
