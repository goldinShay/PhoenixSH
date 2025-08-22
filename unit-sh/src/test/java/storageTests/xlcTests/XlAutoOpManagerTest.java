package storageTests.xlcTests;

import devices.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import sensors.*;
import storage.xlc.XlAutoOpManager;
import storage.xlc.XlWorkbookUtils;
import utils.ClockUtil;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class XlAutoOpManagerTest {

    private static Path tempFile;
    private static final String SHEET_NAME = "Sens_Ctrl";

    @BeforeEach
    void setupWorkbook() throws IOException {
        tempFile = Files.createTempFile("test-sense-", ".xlsx");

        // 🔧 Initialize workbook with required sheet
        try (Workbook workbook = new XSSFWorkbook()) {
            XlWorkbookUtils.createSheetWithHeaders(workbook, "Sens_Ctrl",
                    "SLAVE_TYPE", "SLAVE_ID", "SENSOR_TYPE", "SENSOR_ID", "AUTO_ON", "AUTO_OFF");

            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                workbook.write(fos);
            }
        }

        // Tell your utils to target this file
        XlWorkbookUtils.overrideFilePath(tempFile);
    }


    @Test
    void testAppendToSenseControlSheet() {
        Device light = new Light("LI999", "TestLight", ClockUtil.getClock(), false, 1500, 1450);
        Sensor sensor = new LightSensor("LIs999", "TestSensor", "lux", 1200, ClockUtil.getClock());

        boolean result = XlAutoOpManager.appendToSensCtrlSheet(light, sensor);
        assertTrue(result, "Row should be appended");

        // Reload and check contents
        try (Workbook wb = WorkbookFactory.create(tempFile.toFile())) {
            Sheet sheet = wb.getSheet(SHEET_NAME);
            assertNotNull(sheet);
            assertEquals("LI999", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("LIs999", sheet.getRow(1).getCell(3).getStringCellValue());
        } catch (IOException e) {
            fail("Workbook reload failed: " + e.getMessage());
        }
    }

    @Test
    void testUpdateAutoOpThresholds() {
        // Pre-add device row manually
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(SHEET_NAME);
            Row header = sheet.createRow(0);
            for (int i = 0; i < 6; i++) header.createCell(i);
            Row data = sheet.createRow(1);
            data.createCell(1).setCellValue("LI777");
            data.createCell(4).setCellValue(1000.0);
            data.createCell(5).setCellValue(1000.0);
            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                wb.write(fos);
            }
        } catch (IOException e) {
            fail("Preload failed");
        }

        boolean updated = XlAutoOpManager.updateAutoOpThresholds("LI777", 1600.0, 999.0);
        assertTrue(updated, "Thresholds should update");

        try (Workbook wb = WorkbookFactory.create(tempFile.toFile())) {
            Sheet sheet = wb.getSheet(SHEET_NAME);
            Row row = sheet.getRow(1);
            assertEquals(1600.0, row.getCell(4).getNumericCellValue());
            assertEquals(1600.0, row.getCell(5).getNumericCellValue());
        } catch (IOException e) {
            fail("Reload check failed");
        }
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(tempFile);
    }
    @AfterAll
    static void resetWorkbookPath() {
        // Reset to default path after test run
        XlWorkbookUtils.overrideFilePath(Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx"));
    }

}
