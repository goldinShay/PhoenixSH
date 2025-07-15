package uiTests;

import devices.*;
import org.junit.jupiter.api.*;
import sensors.*;
import storage.*;
import ui.DeviceMenu;

import java.io.*;
import java.time.Clock;
import java.util.*;
import ui.DeviceViewer;

import static org.junit.jupiter.api.Assertions.*;

class DeviceMenuTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    private final Map<String, Device> testDevices = new HashMap<>();
    private final List<Thread> dummyThreads = new ArrayList<>();

    @BeforeEach
    void setup() {
        out.reset();
        System.setOut(new PrintStream(out, true));
        System.setIn(new ByteArrayInputStream("\n".repeat(20).getBytes()));

        DeviceStorage.clear();
        SensorStorage.clear();
        testDevices.clear();
        dummyThreads.clear();

        DeviceViewer.setDisplayHook(null);
        XlCreator.setDeviceUpdater(d -> false);
        XlCreator.setSensorUpdater(s -> false);
        XlCreator.setDeviceRemover(id -> false);
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        out.reset();

        DeviceStorage.clear();
        SensorStorage.clear();
        testDevices.clear();
        dummyThreads.clear();

        XlCreator.setDeviceUpdater(d -> false);
        XlCreator.setSensorUpdater(s -> false);
        XlCreator.setDeviceRemover(id -> false);
        DeviceViewer.setDisplayHook(null);
    }

    @Test
    void whenInvalidChoiceEntered_menuShowsError() {
        String paddedInput = String.join("\n", "foo", "5", "", "", "", "", "");
        System.setIn(new ByteArrayInputStream(paddedInput.getBytes()));

        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        String output = out.toString();
        assertTrue(output.contains("❌ Invalid option. Please choose 1-5."),
                "Expected error message not found in output:\n" + output);
    }

    @Test
    void whenChoosingBack_MenuLoopsOnceAndExits() {
        System.setIn(new ByteArrayInputStream("5\n".getBytes()));
        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        String output = out.toString();
        assertTrue(output.contains("=== Devices Menu ==="));
        assertTrue(output.contains("Back"));
    }

    @Test
    void whenListOptionChosen_displayAllDevicesIsCalled() {
        System.setIn(new ByteArrayInputStream("1\n5\n".getBytes()));

        DeviceViewer.setDisplayHook(() -> System.out.println("🎯 displayAllDevicesAndSensors() CALLED"));

        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        assertTrue(out.toString().contains("🎯 displayAllDevicesAndSensors() CALLED"));
    }

    @Test
    void whenRemoveDeviceOptionUsed_deviceIsRemoved() {
        String id = "LGHT_" + UUID.randomUUID();
        Device dev = new Light(id, "Lamp", Clock.systemUTC(), true, 100.0, 75.0);
        DeviceStorage.getDevices().put(id, dev);

        XlCreator.setDeviceRemover(removalId -> removalId.equals(id));

        System.setIn(new ByteArrayInputStream(("4\n" + id + "\n5\n").getBytes()));
        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        assertTrue(out.toString().contains("🗑️ Removed successfully"));
    }

    @Test
    void whenUpdateDeviceThresholdReset_inputResetsCorrectly() {
        String id = "LGHT_" + UUID.randomUUID();
        Device device = new Light(id, "Lamp", Clock.systemUTC(), true, 100.0, 75.0);
        device.setAutoOnThreshold(200.0, true);
        DeviceStorage.getDevices().put(id, device);

        XlCreator.setDeviceUpdater(d -> true);
        System.setIn(new ByteArrayInputStream((
                "3\n" +
                        id + "\n" +
                        "Updated\n" +
                        "NewBrand\n" +
                        "NewModel\n" +
                        "reset\n" +
                        "5\n").getBytes()));

        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        assertEquals("Updated", device.getName());
        assertEquals("NewBrand", device.getBrand());
        assertEquals("NewModel", device.getModel());
        assertTrue(out.toString().contains("✅ Device updated."));
    }

    @Test
    void whenSensorUpdated_valuesAreStoredAndSynced() {
        String id = "LSR_" + UUID.randomUUID();
        Sensor sensor = new LightSensor("Sensor_" + UUID.randomUUID(), "TestSensor", "lux", 100, Clock.systemUTC());
        SensorStorage.getSensors().put(id, sensor);

        XlCreator.setSensorUpdater(s -> true);

        System.setIn(new ByteArrayInputStream((
                "3\n" +
                        id + "\n" +
                        "Sun\n" +
                        "lumens\n" +
                        "888\n" +
                        "5\n").getBytes()));

        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        assertEquals("Sun", sensor.getSensorName());
        assertEquals("lumens", sensor.getUnit());
        assertEquals(888, sensor.getCurrentValue());
    }

    @Test
    void whenInvalidThresholdIsEntered_valueIsNotChanged() {
        String id = "LGHT_" + UUID.randomUUID();
        Device d = new Light(id, "Lamp", Clock.systemUTC(), true, 100.0, 75.0);
        d.setAutoOnThreshold(500.0, true);
        DeviceStorage.getDevices().put(id, d);

        XlCreator.setDeviceUpdater(dev -> true);
        System.setIn(new ByteArrayInputStream((
                "3\n" + id + "\n\n\n\nnotanumber\n5\n").getBytes()));

        DeviceMenu.DevicesMenu(testDevices, dummyThreads);

        assertEquals(500.0, d.getAutoOnThreshold());
        assertTrue(out.toString().contains("⚠️ Invalid threshold"));
    }
}
