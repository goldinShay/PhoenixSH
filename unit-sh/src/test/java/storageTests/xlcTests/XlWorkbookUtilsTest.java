package storageTests.xlcTests;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import storage.DeviceStorage;
import storage.xlc.XlWorkbookUtils;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class XlWorkbookUtilsTest {

    private static Path testFile;
    private static final Path PROD_XLSX_PATH = Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx");

    @BeforeAll
    static void setupTestWorkbook() throws IOException {
        testFile = Files.createTempFile("test-xlsx-", ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            XlWorkbookUtils.createSheetWithHeaders(workbook, "Devices", "TYPE", "ID", "NAME");
            try (FileOutputStream fos = new FileOutputStream(testFile.toFile())) {
                workbook.write(fos);
            }
        }
    }

    @BeforeEach
    void setup() throws IOException {
        DeviceStorage.clear(); // 🧼 Reset memory

        Path isolatedFile = Files.createTempFile("test-device-", ".xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            XlWorkbookUtils.createSheetWithHeaders(wb, "Devices",
                    "TYPE", "ID", "NAME", "BRAND", "MODEL", "AUTO", "ON", "OFF");

            Row row = wb.getSheet("Devices").createRow(1);
            row.createCell(0).setCellValue("LIGHT");
            row.createCell(1).setCellValue("LI099");
            row.createCell(2).setCellValue("TestLight");
            row.createCell(3).setCellValue("BrandX");
            row.createCell(4).setCellValue("ModelY");
            row.createCell(5).setCellValue(true);
            row.createCell(6).setCellValue(1234);
            row.createCell(7).setCellValue(1300);

            try (FileOutputStream fos = new FileOutputStream(isolatedFile.toFile())) {
                wb.write(fos);
            }
        }

        XlWorkbookUtils.overrideFilePath(isolatedFile);
    }


    @AfterAll
    static void cleanupAndRestoreProdWorkbook() throws IOException {
        Files.deleteIfExists(testFile);
        XlWorkbookUtils.overrideFilePath(PROD_XLSX_PATH);
    }

    @Test
    void testIsExcelFileHealthy() {
        File file = testFile.toFile();
        assertTrue(XlWorkbookUtils.isExcelFileHealthy(file), "File should be healthy");
    }

    @Test
    void testGetWorkbookReadsCorrectly() throws IOException {
        try (Workbook wb = XlWorkbookUtils.getWorkbook(testFile.toString())) {
            assertNotNull(wb.getSheet("Devices"), "Should contain Devices sheet");
        }
    }

    @Test
    void testCreateSheetWithHeaders() {
        try (Workbook wb = new XSSFWorkbook()) {
            XlWorkbookUtils.createSheetWithHeaders(wb, "Sensors", "ID", "TYPE", "VALUE");
            Sheet sheet = wb.getSheet("Sensors");
            Row header = sheet.getRow(0);
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("TYPE", header.getCell(1).getStringCellValue());
            assertEquals("VALUE", header.getCell(2).getStringCellValue());
        } catch (IOException e) {
            fail("Workbook creation failed: " + e.getMessage());
        }
    }

    @Test
    void testSetAndGetCellValue() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Test");
        Row row = sheet.createRow(0);

        XlWorkbookUtils.setCell(row, 0, "hello");
        XlWorkbookUtils.setCell(row, 1, true);
        XlWorkbookUtils.setCell(row, 2, 42.0);

        assertEquals("hello", XlWorkbookUtils.getCellValue(row, 0));
        assertEquals("true", XlWorkbookUtils.getCellValue(row, 1));
        assertEquals("42.0", XlWorkbookUtils.getCellValue(row, 2));
    }
}
