package sensorsTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensors.LightSensor;
import sensors.Sensor;
import sensors.SensorFactory;
import sensors.SensorType;

import java.time.Clock;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SensorFactoryTest {

    private final Clock testClock = Clock.systemDefaultZone();

    @BeforeEach
    void clearRegistry() {
        SensorFactory.getSensors().clear();
    }

    @Test
    void createSensor_shouldReturnCorrectSubclass() {
        Sensor sensor = SensorFactory.createSensor(
                SensorType.LIGHT,
                "S01",
                "LivingRoomSensor",
                "lux",
                150,
                testClock
        );

        assertNotNull(sensor);
        assertEquals("S01", sensor.getSensorId());
        assertTrue(sensor instanceof LightSensor);
    }

    @Test
    void createSensorByType_shouldParseTypeAndReturnSensor() {
        Sensor sensor = SensorFactory.createSensorByType(
                "light",
                "S02",
                "KitchenSensor",
                testClock
        );

        assertNotNull(sensor);
        assertEquals("KitchenSensor", sensor.getSensorName());
        assertTrue(sensor instanceof LightSensor);
    }

    @Test
    void createSensorByType_withInvalidType_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SensorFactory.createSensorByType("TEMPEST", "S03", "FailSensor", testClock);
        });

        assertTrue(exception.getMessage().contains("Invalid or unsupported sensor type"));
    }

    @Test
    void registerSensor_shouldStoreSensorInRegistry() {
        Sensor sensor = SensorFactory.createSensor(
                SensorType.LIGHT, "S04", "GarageSensor", "lux", 180, testClock
        );

        SensorFactory.registerSensor(sensor);

        assertEquals(sensor, SensorFactory.getSensor("S04"));
    }

    @Test
    void getSensors_shouldReturnLiveRegistry() {
        Sensor sensor = SensorFactory.createSensor(
                SensorType.LIGHT, "S05", "HallwaySensor", "lux", 110, testClock
        );

        SensorFactory.registerSensor(sensor);
        Map<String, Sensor> map = SensorFactory.getSensors();

        assertTrue(map.containsKey("S05"));
        assertEquals("HallwaySensor", map.get("S05").getSensorName());
    }
}
