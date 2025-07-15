package sensors;

import java.time.Clock;

@FunctionalInterface
public interface SensorCreator {
    Sensor create(SensorType type, String id, String name, String unit, int defaultVal, Clock clock);
}
