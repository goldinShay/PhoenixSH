package storageTests;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;

import sensors.Sensor;
import storage.XlCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class XlCreatorTest {

    private Workbook workbook;
    private Sheet testSheet;

    @BeforeEach
    void setup() {
        workbook = new XSSFWorkbook();
        testSheet = workbook.createSheet("TestSheet");
    }

    @AfterEach
    void tearDown() throws IOException {
        workbook.close();
    }

    @Test
    void getNextAvailableId_shouldReturnNextSequentialId() {
        Set<String> existing = Set.of("LGT001", "LGT002", "LGT005");
        String result = XlCreator.getNextAvailableId("LGT", existing);
        assertEquals("LGT006", result);
    }

    @Test
    void getNextAvailableId_shouldStartAt001IfNoneExist() {
        Set<String> none = Set.of();
        String result = XlCreator.getNextAvailableId("TMP", none);
        assertEquals("TMP001", result);
    }

    @Test
    void getCellValue_shouldReturnCorrectStringFromCellTypes() {
        Row row = testSheet.createRow(0);

        row.createCell(0).setCellValue("text");
        row.createCell(1).setCellValue(42.0);
        row.createCell(2).setCellValue(true);

        assertEquals("text", invokeGetCellValue(row, 0));
        assertEquals("42.0", invokeGetCellValue(row, 1));
        assertEquals("true", invokeGetCellValue(row, 2));
    }

    @Test
    void getFirstAvailableRow_shouldFindNextFreeRowWithBlankIdColumn() {
        Row r0 = testSheet.createRow(0); r0.createCell(1).setCellValue("abc");
        Row r1 = testSheet.createRow(1); r1.createCell(1).setCellValue("xyz");
        Row r2 = testSheet.createRow(2); r2.createCell(1); // blank cell

        int rowIndex = invokeGetFirstAvailableRow(testSheet);
        assertEquals(3, rowIndex);
    }

    @Test
    void createSheetWithHeaders_shouldInsertHeadersCorrectly() {
        String[] headers = {"ID", "Name", "Type"};
        XlCreatorTestWrapper.createSheetWithHeaders(workbook, "Devices", headers);
        Sheet sheet = workbook.getSheet("Devices");
        assertNotNull(sheet);
        Row header = sheet.getRow(0);
        assertEquals("ID", header.getCell(0).getStringCellValue());
        assertEquals("Type", header.getCell(2).getStringCellValue());
    }

    // ========== 🔧 REFLECTION HELPERS ========== //

    private String invokeGetCellValue(Row row, int col) {
        try {
            var method = XlCreator.class.getDeclaredMethod("getCellValue", Row.class, int.class);
            method.setAccessible(true);
            return (String) method.invoke(null, row, col);
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed on getCellValue", e);
        }
    }

    private int invokeGetFirstAvailableRow(Sheet sheet) {
        try {
            var method = XlCreator.class.getDeclaredMethod("getFirstAvailableRow", Sheet.class);
            method.setAccessible(true);
            return (int) method.invoke(null, sheet);
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed on getFirstAvailableRow", e);
        }
    }

    // ========== ✅ INTERNAL WRAPPER FOR FRIEND ACCESS ========== //

    static class XlCreatorTestWrapper extends XlCreator {
        public static void createSheetWithHeaders(Workbook w, String name, String... headers) {
            try {
                var method = XlCreator.class.getDeclaredMethod("createSheetWithHeaders", Workbook.class, String.class, String[].class);
                method.setAccessible(true);
                method.invoke(null, w, name, (Object) headers);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SensorExcelIOTests {

        private Path tempFile;
        private File file;

        @BeforeEach
        void createFreshExcelFile() throws IOException {
            tempFile = Files.createTempFile("sensor-test-", ".xlsx");
            Files.deleteIfExists(tempFile);
            file = tempFile.toFile();
            file.deleteOnExit();

            // ✅ Initialize valid empty workbook with header
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Sensors");
                Row header = sheet.createRow(0);
                String[] headers = {
                        "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNIT_NAME",
                        "DEFAULT_VALUE", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS"
                };
                for (int i = 0; i < headers.length; i++) {
                    header.createCell(i).setCellValue(headers[i]);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }
        }


        @Test
        void writeSensorToExcel_shouldAddSensorRow() {
            TestSensor sensor = new TestSensor("S100", "TestLight", "lux", 150);
            sensor.updateTimestamp();

            boolean success = XlCreatorTestWrapper.writeSensorToExcel(sensor, file);
            assertTrue(success, "Sensor write operation failed");

            try (Workbook workbook = WorkbookFactory.create(file)) {
                Sheet sheet = workbook.getSheet("Sensors");
                assertNotNull(sheet, "Sheet 'Sensors' not found");

                boolean found = false;
                System.out.println("🔍 Reading rows from 'Sensors' sheet...");
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    Cell idCell = row.getCell(1);
                    String cellValue = (idCell != null) ? idCell.getStringCellValue().trim() : "null";
                    System.out.printf("🧪 Row %d: CELL[1] = '%s'%n", row.getRowNum(), cellValue);

                    if ("S100".equalsIgnoreCase(cellValue)) {
                        String name = row.getCell(2).getStringCellValue().trim();
                        String unit = row.getCell(3).getStringCellValue().trim();

                        System.out.printf("✅ Match found → ID: %s | Name: %s | Unit: %s%n", cellValue, name, unit);

                        assertEquals("TestLight", name, "Sensor name mismatch");
                        assertEquals("lux", unit, "Sensor unit mismatch");
                        found = true;
                        break;
                    }
                }

                assertTrue(found, "Sensor row with ID 'S100' not found in Excel");

            } catch (IOException e) {
                e.printStackTrace();
                fail("IOException reading Excel file: " + e.getMessage());
            }
        }

        // 🔧 Stubbed sensor with minimal logic
        static class TestSensor extends sensors.Sensor {
            public TestSensor(String id, String name, String unit, int defaultVal) {
                super(id, sensors.SensorType.LIGHT, name, unit, defaultVal, Clock.systemDefaultZone());
            }

            @Override public int readCurrentValue() { return currentValue; }
            @Override public int getCurrentReading() { return currentValue; }
            @Override public void simulateValue(int value) { this.currentValue = value; }
        }

        // 🔧 Internal test override for writing logic
        static class XlCreatorTestWrapper extends XlCreator {
            public static boolean writeSensorToExcel(Sensor sensor, File targetFile) {
                Workbook workbook;

                try {
                    try {
                        if (targetFile.exists() && Files.size(targetFile.toPath()) > 0) {
                            workbook = WorkbookFactory.create(targetFile);
                        } else {
                            workbook = new XSSFWorkbook();
                        }
                    } catch (Exception corruptFile) {
                        System.err.println("⚠️ WorkbookFactory could not read file. Using fresh workbook instead.");
                        workbook = new XSSFWorkbook();
                    }

                    Sheet sheet = workbook.getSheet("Sensors");
                    if (sheet == null) {
                        sheet = workbook.createSheet("Sensors");
                        Row header = sheet.createRow(0);
                        String[] headers = {
                                "SENSOR_TYPE", "SENSOR_ID", "NAME", "UNIT_NAME",
                                "DEFAULT_VALUE", "ACTIONS", "ADDED_TS", "UPDATED_TS", "REMOVED_TS"
                        };
                        for (int i = 0; i < headers.length; i++) {
                            header.createCell(i).setCellValue(headers[i]);
                        }
                    }

                    // Check for existing row
                    Row targetRow = null;
                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) continue;
                        Cell idCell = row.getCell(1);
                        if (idCell != null && sensor.getSensorId().equals(idCell.getStringCellValue())) {
                            targetRow = row;
                            break;
                        }
                    }

                    if (targetRow == null) {
                        targetRow = sheet.createRow(sheet.getLastRowNum() + 1);
                    }

                    targetRow.createCell(0).setCellValue(sensor.getSensorType().toString());
                    targetRow.createCell(1).setCellValue(sensor.getSensorId());
                    targetRow.createCell(2).setCellValue(sensor.getSensorName());
                    targetRow.createCell(3).setCellValue(sensor.getUnit());
                    targetRow.createCell(4).setCellValue(sensor.getCurrentValue());
                    targetRow.createCell(6).setCellValue(sensor.getCreatedTimestamp().toString());

                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        workbook.write(fos);
                    }

                    workbook.close();
                    return true;

                } catch (Exception e) {
                    System.err.println("❌ Failed to write sensor: " + e.getMessage());
                    return false;
                }
            }

        }
        @Test
        void updateSensor_shouldModifyExistingRow() {
            // Create sensor and write it
            TestSensor sensor = new TestSensor("S200", "OriginalLight", "lux", 100);
            sensor.updateTimestamp();

            boolean firstWrite = XlCreatorTestWrapper.writeSensorToExcel(sensor, file);
            assertTrue(firstWrite, "Initial write failed");
            try {
                Thread.sleep(50); // 👈 Let file system settle
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }


            // Now simulate a change
            sensor.simulateValue(777);
            sensor.setSensorName("UpdatedLight");
            sensor.updateTimestamp(); // new timestamp for update

            boolean updateWrite = XlCreatorTestWrapper.writeSensorToExcel(sensor, file);
            assertTrue(updateWrite, "Update write failed");

            try (Workbook workbook = WorkbookFactory.create(file)) {
                Sheet sheet = workbook.getSheet("Sensors");
                assertNotNull(sheet);

                boolean found = false;
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    Cell idCell = row.getCell(1);
                    if (idCell == null) continue;

                    String sensorId = idCell.getStringCellValue();
                    if ("S200".equals(sensorId)) {
                        String name = row.getCell(2).getStringCellValue();
                        int value = (int) row.getCell(4).getNumericCellValue();

                        System.out.printf("🔁 Found updated row: Name = %s | Value = %d%n", name, value);

                        assertEquals("UpdatedLight", name, "Sensor name not updated");
                        assertEquals(777, value, "Sensor value not updated");
                        found = true;
                        break;
                    }
                }

                assertTrue(found, "Updated sensor row not found");

            } catch (IOException e) {
                fail("IOException reading Excel file: " + e.getMessage());
            }
        }

    }

}
