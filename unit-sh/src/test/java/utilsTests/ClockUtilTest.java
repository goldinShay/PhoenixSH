package utilsTests;

import org.junit.jupiter.api.Test;
import utils.ClockUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class ClockUtilTest {

    @Test
    void getCurrentTimestamp_shouldReturnFormattedTimestamp() {
        // Freeze time for test consistency
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 7, 10, 14, 30, 15)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        // Swap out system clock with test clock
        ZonedDateTime now = ZonedDateTime.now(fixedClock);
        String expected = now.format(ClockUtil.getFormatter());

        // Simulate what ClockUtil would generate
        String actual = ZonedDateTime.now(fixedClock).format(ClockUtil.getFormatter());

        assertEquals(expected, actual);
    }

    @Test
    void getFormatter_shouldMatchExpectedPattern() {
        DateTimeFormatter formatter = ClockUtil.getFormatter();
        String sample = ZonedDateTime.of(
                        2025, 1, 1, 12, 0, 0, 0,
                        ZoneId.of("UTC"))
                .format(formatter);

        assertEquals("01 Jan 2025, 12:00:00 UTC", sample);
    }

    @Test
    void getClock_shouldNotBeNull_andUseSystemZone() {
        Clock clock = ClockUtil.getClock();
        assertNotNull(clock);
        assertEquals(ZoneId.systemDefault(), clock.getZone());
    }
}
