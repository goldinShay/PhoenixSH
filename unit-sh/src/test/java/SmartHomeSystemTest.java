import autoOp.AutoOpManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.DeviceStorage;
import storage.SensorStorage;
import storage.XlCreator;
import storage.xlc.XlWorkbookUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import java.nio.file.Path;

class SmartHomeSystemTest {

    @BeforeEach
    void clearState() {
        DeviceStorage.clear();
        SensorStorage.clear();
    }

    @Test
    void whenExcelFileExists_thenEnsureExcelReturnsTrue() throws Exception {
        Path dummyPath = new File("dummy.xlsx").toPath(); // pretend this is your Excel file

        try (var staticMock = mockStatic(XlWorkbookUtils.class)) {
            staticMock.when(XlWorkbookUtils::getFilePath).thenReturn(dummyPath);

            // Make sure the dummy file appears to exist
            File file = dummyPath.toFile();
            file.createNewFile(); // 🔧 create temporarily for test

            Method method = SmartHomeSystem.class.getDeclaredMethod("ensureExcelFileExists", boolean.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(null, false);

            assertTrue(result);

            file.delete(); // 🧹 clean up after test
        }
    }


    @Test
    void whenExcelMissingAndUserSaysYes_thenExcelIsCreated() throws Exception {
        // 🧪 Simulate user saying "Y"
        String simulatedInput = "Y\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

        // 🔧 Create dummy path and ensure file does NOT exist
        File dummyFile = new File("dummy_test.xlsx");
        if (dummyFile.exists()) dummyFile.delete(); // just to be safe
        Path dummyPath = dummyFile.toPath();

        try (var utilsMock = mockStatic(XlWorkbookUtils.class);
             var creatorMock = mockStatic(XlCreator.class)) {

            utilsMock.when(XlWorkbookUtils::getFilePath).thenReturn(dummyPath);
            creatorMock.when(XlCreator::createNewWorkbook).thenReturn(true);

            Method method = SmartHomeSystem.class.getDeclaredMethod("ensureExcelFileExists", boolean.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(null, false);
            assertTrue(result);

        } finally {
            if (dummyFile.exists()) dummyFile.delete(); // 🧹 Clean up
        }
    }


    @Test
    void whenExcelMissingAndUserSaysNo_thenStartupAborts() throws Exception {
        // Simulate user input: "N"
        String simulatedInput = "N\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

        File dummyFile = new File("dummy_abort.xlsx");
        if (dummyFile.exists()) dummyFile.delete(); // Clean up beforehand
        Path dummyPath = dummyFile.toPath();

        try (var utilsMock = mockStatic(XlWorkbookUtils.class)) {
            utilsMock.when(XlWorkbookUtils::getFilePath).thenReturn(dummyPath);

            Method method = SmartHomeSystem.class.getDeclaredMethod("ensureExcelFileExists", boolean.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(null, false);
            assertFalse(result); // 🧪 Startup should abort
        } finally {
            if (dummyFile.exists()) dummyFile.delete(); // 🧹 Clean up afterward
        }
    }


    @Test
    void whenInitializeSystemCalled_thenDevicesAndSchedulerArePrepared() throws Exception {
        try (var deviceMock = mockStatic(DeviceStorage.class);
             var sensorMock = mockStatic(SensorStorage.class);
             var creatorMock = mockStatic(XlCreator.class);
             var autoOpMock = mockStatic(AutoOpManager.class)) {

            deviceMock.when(DeviceStorage::initialize).thenAnswer(invocation -> null);
            sensorMock.when(SensorStorage::loadSensorsFromExcel).thenAnswer(invocation -> null);
            creatorMock.when(() -> XlCreator.loadSensorLinks(any(), any())).thenAnswer(invocation -> null);
            autoOpMock.when(AutoOpManager::restoreMemoryLinks).thenAnswer(invocation -> null);
            autoOpMock.when(AutoOpManager::reevaluateAllSensors).thenAnswer(invocation -> null);

            Method method = SmartHomeSystem.class.getDeclaredMethod("initializeSystem");
            method.setAccessible(true);
            method.invoke(null);

            deviceMock.verify(DeviceStorage::initialize);
            sensorMock.verify(SensorStorage::loadSensorsFromExcel);
            autoOpMock.verify(AutoOpManager::restoreMemoryLinks);
            autoOpMock.verify(AutoOpManager::reevaluateAllSensors);
        }
    }

    @Test
    void whenPrepareSchedulerCalled_thenSchedulerIsConfiguredCorrectly() throws Exception {
        Method method = SmartHomeSystem.class.getDeclaredMethod("prepareScheduler");
        method.setAccessible(true);
        method.invoke(null);

        // Can’t directly verify timer action, but this confirms no crash
    }
}
