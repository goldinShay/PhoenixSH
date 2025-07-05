package sensorsTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensors.LightSensor;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LightSensorTest {

    private LightSensor sensor;
    private TestLight autoLight;
    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests(); // 💥 THIS LINE

        sensor = new LightSensor("S001", "Test LightSensor", "lx", 100, Clock.systemDefaultZone());
        autoLight = new TestLight("L01", "AutoLight");
        sensor.addSlave(autoLight);
    }
    // 🧪 Test stub for a Device (acts like a Light)
    static class TestLight extends Device {
        private boolean turnedOn = false;
        private boolean turnedOff = false;

        public TestLight(String id, String name) {
            super(id, name, DeviceType.LIGHT, Clock.systemDefaultZone(), 500.0, 800.0);
            this.setAutomationEnabled(true);
        }


        @Override
        public List<String> getAvailableActions() {
            return List.of("on", "off");
        }

        @Override
        public void simulate(String action) {
            if ("on".equalsIgnoreCase(action)) turnOn();
            if ("off".equalsIgnoreCase(action)) turnOff();
        }

        @Override
        public void turnOn() {
            super.turnOn();
            turnedOn = true;
        }

        @Override
        public void turnOff() {
            super.turnOff();
            turnedOff = true;
        }

        public boolean wasTurnedOn() { return turnedOn; }
        public boolean wasTurnedOff() { return turnedOff; }
    }
    @Test
    void whenSimulatingLowValueAndLightIsOff_shouldTurnOn() {
        autoLight.turnOff();
        sensor.simulateValue(200); // below 500 threshold
        assertTrue(autoLight.wasTurnedOn());
    }

    @Test
    void whenSimulatingHighValueAndLightIsOn_shouldTurnOff() {
        autoLight.turnOn();
        sensor.simulateValue(900); // above 800 threshold
        assertTrue(autoLight.wasTurnedOff());
    }

    @Test
    void getCurrentReading_shouldReflectSimulatedValue() {
        sensor.simulateValue(333);
        assertEquals(333, sensor.getCurrentReading());
    }

    @Test
    void toString_shouldContainSensorNameAndCurrentValue() {
        sensor.simulateValue(123);
        String result = sensor.toString();
        assertTrue(result.contains("Test LightSensor"));
        assertTrue(result.contains("123"));
    }
}
