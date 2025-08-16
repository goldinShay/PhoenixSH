package utils;

import java.time.Clock;
import java.time.ZonedDateTime;

public class TimestampUtils {

    public static ZonedDateTime safeParseTimestamp(String ts, Clock clock) {
        return (ts != null && !ts.isBlank())
                ? ZonedDateTime.parse(ts)
                : ZonedDateTime.now(clock);
    }
}