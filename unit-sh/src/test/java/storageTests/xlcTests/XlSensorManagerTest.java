package storageTests.xlcTests;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import sensors.*;
import storage.xlc.XlSensorManager;
import storage.xlc.XlWorkbookUtils;

import java.io.*;
import java.nio.file.*;
import java.time.Clock;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class XlSensorManagerTest {

    private Path tempFile;
    private final Clock clock = Clock.systemUTC();
    private static final String SHEET_NAME = "Sensors";

    @BeforeEach
    void setupWorkbook() throws IOException {
        tempFile = Files.createTempFile("sensor-test-", ".xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sensors");

            Row header = sheet.createRow(0);
            String[] headers = {
                    "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNITS",
                    "DEFAULT_VAL", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                wb.write(fos); // 🔥 Actually writes content into the file
            }
        }

        XlWorkbookUtils.overrideFilePath(tempFile);
    }

    @Test
    void testUpdateSensor() {
        Sensor sensor = new LightSensor("S001", "OriginalName", "lux", 100, clock);
        assertTrue(XlSensorManager.writeSensorToExcel(sensor));

        // Modify values
        sensor.setSensorName("UpdatedName");
        sensor.unit = "lumens";
        sensor.simulateValue(150);

        assertTrue(XlSensorManager.updateSensor(sensor), "Sensor should update");

        Map<String, Sensor> loaded = XlSensorManager.loadSensors();
        Sensor reloaded = loaded.get("S001");
        assertNotNull(reloaded);
        assertEquals("UpdatedName", reloaded.getSensorName());
        assertEquals("lumens", reloaded.getUnit());
        assertEquals(150, reloaded.getCurrentValue());
    }
    @Test
    void testWriteSensorToExcel() {
        Sensor tempSensor = new LightSensor("T001", "TempSensor", "lux", 25, clock);
        boolean result = XlSensorManager.writeSensorToExcel(tempSensor);
        assertTrue(result, "Sensor should be written to Excel");

        Map<String, Sensor> loaded = XlSensorManager.loadSensors();
        assertTrue(loaded.containsKey("T001"));
        assertEquals("TempSensor", loaded.get("T001").getSensorName());
    }

    @Test
    void testRemoveSensor() {
        Sensor sensor = new LightSensor("S001", "OriginalName", "lux", 100, clock);
        assertTrue(XlSensorManager.writeSensorToExcel(sensor));
        assertTrue(XlSensorManager.removeSensor("H001"), "Sensor should be removed");

        Map<String, Sensor> loaded = XlSensorManager.loadSensors();
        assertFalse(loaded.containsKey("H001"));
    }

    @Test
    void testLoadInvalidSensorRowSkipsGracefully() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(SHEET_NAME);

            // Headers (optional)
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("SENSOR_TYPE");
            header.createCell(1).setCellValue("SENSOR_ID");
            header.createCell(2).setCellValue("NAME");

            // Row missing critical fields (type + ID)
            Row row = sheet.createRow(1);
            row.createCell(2).setCellValue("Unnamed");

            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                wb.write(fos);
            }
        }

        Map<String, Sensor> loaded = XlSensorManager.loadSensors();
        assertTrue(loaded.isEmpty(), "Broken row should be skipped with no sensors loaded");
    }


    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @AfterAll
    static void resetWorkbookPath() {
        XlWorkbookUtils.overrideFilePath(Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx"));
    }
}
