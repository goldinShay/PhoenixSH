import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ClockUtil {

    // Custom timestamp format
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss z");

    // Shared Clock instance
    private static final Clock clock = Clock.systemDefaultZone();

    // Returns formatted current timestamp using the shared Clock
    public static String getCurrentTimestamp() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return now.format(formatter);
    }

    // Expose the shared Clock
    public static Clock getClock() {
        return clock;
    }
}
