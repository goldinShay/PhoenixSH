package uiTests;

import org.junit.jupiter.api.*;
import sensors.*;
import storage.SensorStorage;
import storage.xlc.XlWorkbookUtils;
import ui.AddSensorMenu;

import java.io.*;
import java.nio.file.*;
import java.time.Clock;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AddSensorMenuTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    private Path tempFile;

    @BeforeEach
    void setup() throws IOException {
        System.setOut(new PrintStream(outContent));
        SensorStorage.clear();

        tempFile = Files.createTempFile("sensor-menu-test-", ".xlsx");
        try (var wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            String[] sheets = { "Sensors", "Devices", "Sense_Control", "Scheduled Tasks" };
            for (String name : sheets) wb.createSheet(name);
            try (var fos = new FileOutputStream(tempFile.toFile())) {
                wb.write(fos);
            }
        }

        XlWorkbookUtils.overrideFilePath(tempFile);
    }

    @AfterEach
    void teardown() throws IOException {
        System.setOut(originalOut);
        System.setIn(originalIn);
        SensorFactory.resetSensorCreator(); // 🔄 Important for test isolation
        SensorStorage.clear();
        Files.deleteIfExists(tempFile);
    }

    @AfterAll
    static void resetPath() {
        XlWorkbookUtils.overrideFilePath(Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx"));
    }

    @Test
    void whenValidInputProvided_sensorIsCreatedSuccessfully() {
        System.setIn(new ByteArrayInputStream("1\n1\n123\n".getBytes()));
        AddSensorMenu.run("WeatherWatcher");

        assertEquals(1, SensorStorage.getSensors().size());
        Sensor sensor = SensorStorage.getSensors().values().iterator().next();

        assertEquals("WeatherWatcher", sensor.getSensorName());
        assertEquals(123, sensor.getCurrentValue());
        assertTrue(sensor.getSensorId().matches("LIs\\d{3}"));
        assertTrue(outContent.toString().contains("✅ Sensor 'WeatherWatcher"));
    }

    @Test
    void whenSensorTypeInvalid_inputFailsGracefully() {
        System.setIn(new ByteArrayInputStream("99\n\n\n".getBytes())); // padded
        AddSensorMenu.run("InvalidType");

        assertTrue(outContent.toString().contains("❌ Sensor creation cancelled"));
        assertTrue(SensorStorage.getSensors().isEmpty());
    }

    @Test
    void whenMeasurementUnitInvalid_creationIsAborted() {
        System.setIn(new ByteArrayInputStream("1\n0\n\n".getBytes())); // padded
        AddSensorMenu.run("InvalidUnit");

        assertTrue(outContent.toString().contains("❌ Invalid unit selection"));
        assertTrue(SensorStorage.getSensors().isEmpty());
    }

    @Test
    void whenDefaultValueInvalid_sensorIsNotCreated() {
        System.setIn(new ByteArrayInputStream("1\n1\nNaN\n".getBytes()));
        AddSensorMenu.run("BadValue");

        assertTrue(outContent.toString().contains("❌ Invalid default value"));
        assertTrue(SensorStorage.getSensors().isEmpty());
    }

    @Test
    void whenExistingSensorsPresent_nextAvailableIdIsUsed() {
        Sensor existing = new LightSensor("LIs042", "Legacy", "lux", 100, Clock.systemUTC());
        SensorStorage.getSensors().put("LIs042", existing);

        System.setIn(new ByteArrayInputStream("1\n1\n200\n".getBytes()));
        AddSensorMenu.run("FreshLight");

        assertTrue(SensorStorage.getSensors().containsKey("LIs043"));
    }

    @Test
    void whenFactoryThrowsException_userSeesFailureMessage() {
        SensorFactory.setSensorCreator((type, id, name, unit, val, clock) -> {
            throw new RuntimeException("Boom");
        });

        System.setIn(new ByteArrayInputStream("1\n1\n123\n".getBytes()));
        AddSensorMenu.run("Explodo");

        String output = outContent.toString();
        assertTrue(output.contains("❌ Failed to create sensor: Boom"));
        assertTrue(SensorStorage.getSensors().isEmpty());
    }
}
