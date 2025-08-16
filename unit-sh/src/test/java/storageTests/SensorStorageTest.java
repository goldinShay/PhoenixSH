package storageTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensors.Sensor;
import storage.SensorStorage;
import sensors.SensorType;

import java.time.Clock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SensorStorageTest {

    static class TestSensor extends Sensor {
        public TestSensor(String id, String name, String unit, int defaultVal) {
            super(id, SensorType.DEBUG, name, unit, defaultVal, Clock.systemDefaultZone());
        }

        @Override public double readCurrentValue() { return currentValue; }
        @Override public double getCurrentReading() { return currentValue; }
    }

    private TestSensor sensor;

    @BeforeEach
    void setUp() {
        sensor = new TestSensor("S001", "TestSensor", "dB", 42);
        SensorStorage.clear();
    }

    @Test
    void addSensor_shouldStoreSensorInMap() {
        SensorStorage.addSensor(sensor.getSensorId(), sensor);
        assertEquals(sensor, SensorStorage.getSensor("S001"));
    }

    @Test
    void removeSensor_shouldDeleteAndReturnSensor() {
        SensorStorage.addSensor("S001", sensor);
        Sensor removed = SensorStorage.removeSensor("S001");
        assertEquals(sensor, removed);
        assertNull(SensorStorage.getSensor("S001"));
    }

    @Test
    void getUnmodifiableSensors_shouldReflectContentsAndBeImmutable() {
        SensorStorage.addSensor("S001", sensor);
        Map<String, Sensor> readOnlyMap = SensorStorage.getUnmodifiableSensors();
        assertTrue(readOnlyMap.containsKey("S001"));

        assertThrows(UnsupportedOperationException.class, () -> {
            readOnlyMap.put("HAX", sensor);
        });
    }

    @Test
    void getSensors_shouldReturnLiveMapReference() {
        Map<String, Sensor> map = SensorStorage.getSensors();
        map.put("S001", sensor);
        assertEquals(sensor, SensorStorage.getSensor("S001"));
    }

    @Test
    void clear_shouldRemoveAllSensors() {
        SensorStorage.addSensor("S001", sensor);
        SensorStorage.clear();
        assertTrue(SensorStorage.getSensors().isEmpty());
    }
}
