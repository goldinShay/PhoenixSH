package storageTests.xlcTests;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import storage.xlc.XlTaskSchedulerManager;
import storage.xlc.XlWorkbookUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class XlTaskSchedulerManagerTest {

    private Path tempFile;
    private static final String SHEET_NAME = "Scheduled Tasks";

    @BeforeEach
    void setupWorkbook() throws IOException {
        tempFile = Files.createTempFile("tasks-test-", ".xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            // ✅ Create all required sheets
            String[] sheets = { "Scheduled Tasks", "Devices", "Sensors", "Sense_Control" };

            for (String name : sheets) {
                Sheet sheet = wb.createSheet(name);

                // Only "Scheduled Tasks" needs actual headers
                if (name.equals("Scheduled Tasks")) {
                    Row header = sheet.createRow(0);
                    String[] columns = { "DEVICE_ID", "DEVICE_NAME", "ACTION", "SCHEDULED", "REPEAT" };
                    for (int i = 0; i < columns.length; i++) {
                        header.createCell(i).setCellValue(columns[i]);
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                wb.write(fos);
            }
        }

        XlWorkbookUtils.overrideFilePath(tempFile);
    }
    @Test
    void testDeleteTask() {
        XlTaskSchedulerManager.addTask("D999", "KillSwitch", "ON", "2030-10-20T10:10", "NEVER");

        boolean deleted = XlTaskSchedulerManager.deleteTask("D999");
        assertTrue(deleted, "Task should be deleted");

        List<Map<String, String>> tasks = XlTaskSchedulerManager.loadTasks();
        assertTrue(tasks.stream().noneMatch(t -> t.get("DEVICE_ID").equals("D999")));
    }
    @Test
    void testLoadTasksEmptySheet() {
        List<Map<String, String>> tasks = XlTaskSchedulerManager.loadTasks();
        assertTrue(tasks.isEmpty(), "Empty file should return empty list");
    }



    @Test
    void testAddAndLoadTask() {
        boolean added = XlTaskSchedulerManager.addTask("D001", "TestDevice", "ON", "2025-07-07T15:00", "DAILY");
        assertTrue(added, "Task should be added");

        List<Map<String, String>> tasks = XlTaskSchedulerManager.loadTasks();
        assertEquals(1, tasks.size());

        Map<String, String> task = tasks.get(0);
        assertEquals("D001", task.get("DEVICE_ID"));
        assertEquals("TestDevice", task.get("DEVICE_NAME"));
        assertEquals("ON", task.get("ACTION"));
        assertEquals("2025-07-07T15:00", task.get("SCHEDULED"));
        assertEquals("DAILY", task.get("REPEAT"));
    }

    @Test
    void testUpdateTask() {
        XlTaskSchedulerManager.addTask("D777", "UpDevice", "OFF", "2025-01-01T08:00", "WEEKLY");

        boolean updated = XlTaskSchedulerManager.updateTask("D777", "2025-12-31T21:45", "ONCE");
        assertTrue(updated, "Task should update successfully");

        List<Map<String, String>> tasks = XlTaskSchedulerManager.loadTasks();
        Map<String, String> task = tasks.stream()
                .filter(t -> t.get("DEVICE_ID").equals("D777"))
                .findFirst()
                .orElseThrow();

        assertEquals("2025-12-31T21:45", task.get("SCHEDULED"));
        assertEquals("ONCE", task.get("REPEAT"));
    }


    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @AfterAll
    static void restorePath() {
        XlWorkbookUtils.overrideFilePath(Paths.get("/home/nira/Documents/Shay/Fleur/unit-sh/unit-sh/shsXl.xlsx"));
    }
}
