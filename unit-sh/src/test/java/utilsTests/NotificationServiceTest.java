package utilsTests;

import org.junit.jupiter.api.*;
import utils.NotificationService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    NotificationService service = new NotificationService();

    @BeforeEach
    void redirectStreams() {
        System.setOut(new PrintStream(outBuffer));
        System.setErr(new PrintStream(errBuffer));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        outBuffer.reset();
        errBuffer.reset();
    }

    @Test
    void whenNotifyCalled_thenMessagePrintedToOut() {
        service.notify("System starting");
        assertTrue(outBuffer.toString().contains("[🔔 NOTIFICATION] System starting"));
    }

    @Test
    void whenDeviceNotifyCalled_thenDeviceMessagePrintedToOut() {
        service.notify("D456", "Temperature rising");
        assertTrue(outBuffer.toString().contains("[🔔 DEVICE D456] Temperature rising"));
    }

    @Test
    void whenNotifySuccessCalled_thenSuccessMessagePrinted() {
        service.notifySuccess("Upload complete");
        assertTrue(outBuffer.toString().contains("[✅ SUCCESS] Upload complete"));
    }

    @Test
    void whenNotifyWarningCalled_thenWarningMessagePrinted() {
        service.notifyWarning("Battery low");
        assertTrue(outBuffer.toString().contains("[⚠️ WARNING] Battery low"));
    }

    @Test
    void whenNotifyErrorCalled_thenErrorMessagePrintedToErr() {
        service.notifyError("Connection failed");
        assertTrue(errBuffer.toString().contains("[❌ ERROR] Connection failed"));
    }
}
