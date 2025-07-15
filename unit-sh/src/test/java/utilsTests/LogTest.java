package utilsTests;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.*;
import utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        outContent.reset();
        errContent.reset();
    }

    @Test
    void info_shouldPrintMessageToOut() {
        Log.info("System is online");
        assertTrue(outContent.toString().contains("ℹ️ System is online"));
    }

    @Test
    void warn_shouldPrintWarningToOut() {
        Log.warn("Low memory");
        assertTrue(outContent.toString().contains("⚠️ Low memory"));
    }

    @Test
    void error_shouldPrintErrorToErr() {
        Log.error("Disk failure");
        assertTrue(errContent.toString().contains("❌ Disk failure"));
    }

    @Test
    void debug_shouldNotPrintInFalseMode() {
        Log.debug("Debug: Unreachable code");
        assertEquals("", outContent.toString()); // DEBUG_MODE = false
    }

    @Test
    void debugf_shouldNotPrintInFalseMode() {
        Log.debugf("Value: %.2f", 42.42);
        assertEquals("", outContent.toString());
    }

    @Test
    void debugSheetNames_shouldNotPrintInFalseMode() {
        Workbook workbook = mock(Workbook.class);
        Log.debugSheetNames(workbook); // DEBUG_MODE is off
        assertEquals("", outContent.toString());
    }
}
