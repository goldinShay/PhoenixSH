package sensors;

import java.time.Clock;

@FunctionalInterface
public interface SensorCreator {
    Sensor create(SensorType type, String id, String name, MeasurementUnit unit, double currentValue, Clock clock);
}
