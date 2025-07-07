package storageTests.xlcTests;

import devices.Device;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.DeviceStorage;
import storage.xlc.XlDeviceManager;
import storage.xlc.XlWorkbookUtils;
import utils.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class XlDeviceManagerTest {

    private static Path testFile;
    private static final String SHEET_NAME = "Devices";

    @BeforeAll
    static void setupWorkbook() throws IOException {
        // Create a temp Excel file for test isolation
        testFile = Files.createTempFile("test-devices", ".xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            Row header = sheet.createRow(0);
            for (int i = 0; i <= 11; i++) header.createCell(i);

            // Add one valid light device row
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("LIGHT");
            row.createCell(1).setCellValue("LI099");
            row.createCell(2).setCellValue("TestLight");
            row.createCell(3).setCellValue("BrandX");
            row.createCell(4).setCellValue("ModelY");
            row.createCell(5).setCellValue(true); // Auto-enabled
            row.createCell(6).setCellValue(1234);
            row.createCell(7).setCellValue(1300);

            try (FileOutputStream fos = new FileOutputStream(testFile.toFile())) {
                workbook.write(fos);
            }
        }

        // Patch XlWorkbookUtils to return our temp file
        XlWorkbookUtils.overrideFilePath(testFile); // <- if you support this via config
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


    @Test
    void testLoadDevicesFromExcel() {
        List<Device> devices = XlDeviceManager.loadDevicesFromExcel();
        assertFalse(devices.isEmpty(), "Should load at least one device");

        Device light = devices.stream()
                .filter(d -> d.getId().equals("LI099"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Device LI099 not found"));

        assertEquals("TestLight", light.getName());
        assertEquals("BrandX", light.getBrand());
        assertTrue(light.isAutomationEnabled());
    }

    @Test
    void testGetNextAvailableId() {
        Set<String> ids = new HashSet<>(List.of("LI001", "LI002", "LI099"));
        String next = XlDeviceManager.getNextAvailableId("LI", ids);
        assertEquals("LI100", next);
    }

    // Additional tests for updateDevice, removeDevice, writeDeviceToExcel can go here
    // Each will re-use or reset the workbook as needed
    @AfterEach
    void resetAfterTest() {
        DeviceStorage.clear();
    }

}
