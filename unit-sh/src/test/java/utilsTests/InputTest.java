package utilsTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Input;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InputTest {

    private final String newline = System.lineSeparator();

    @BeforeEach
    void resetScanner() {
        // reset before each test just in case
        Input.setScanner(new Scanner(System.in));
    }

    @AfterEach
    void restoreDefaultScanner() {
        Input.setScanner(new Scanner(System.in));
    }

    @Test
    void getLine_shouldReturnTrimmedInput() {
        String simulatedInput = "  hello world  " + newline;
        Input.setScanner(new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8))));

        String result = Input.getLine("Prompt: ");
        assertEquals("hello world", result);
    }

    @Test
    void getInt_shouldReturnParsedInteger() {
        String simulatedInput = "42" + newline;
        Input.setScanner(new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8))));

        int result = Input.getInt("Prompt: ");
        assertEquals(42, result);
    }

    @Test
    void getInt_shouldRetryUntilValidNumber() {
        String simulatedInput = "abc" + newline + "99" + newline;
        Input.setScanner(new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8))));

        int result = Input.getInt("Enter number: ");
        assertEquals(99, result);
    }

    @Test
    void getInt_withMinMax_shouldEnforceBounds() {
        String simulatedInput = "5" + newline + "12" + newline;
        Input.setScanner(new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8))));

        int result = Input.getInt("Enter number [10–20]: ", 10, 20);
        assertEquals(12, result);
    }
}
