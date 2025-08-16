package sensorsTests;

import devices.Device;
import devices.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensors.Sensor;
import sensors.SensorType;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SensorTest {

    private TestSensor sensor;
    private MockDevice device;

    // 🔧 Minimal concrete subclass for safe testing
    static class TestSensor extends Sensor {
        public TestSensor(String id, String name, String unit, int defaultValue) {
            super(id, SensorType.LIGHT, name, unit, defaultValue, Clock.systemDefaultZone());
        }

        @Override public double readCurrentValue() { return currentValue; }
        @Override public double getCurrentReading() { return currentValue; }

        @Override public void simulateValue(double value) {
            this.currentValue = value;
            updateTimestamp();
            notifyLinkedDevices(value);
        }
    }

    // 🧪 Mock device to track interactions
    static class MockDevice extends Device {
        public boolean turnedOn = false;
        public boolean turnedOff = false;

        public MockDevice(String id) {
            super(id, "Mock", DeviceType.LIGHT, Clock.systemDefaultZone(), 400.0, 600.0);
            setAutomationEnabled(true);
        }

        @Override public List<String> getAvailableActions() {
            return List.of("on", "off");
        }

        @Override public void simulate(String action) {
            // No-op
        }

        @Override public void turnOn() {
            super.turnOn();
            turnedOn = true;
        }

        @Override public void turnOff() {
            super.turnOff();
            turnedOff = true;
        }
    }

    @BeforeEach
    void setUp() {
        Device.clearDeviceRegistryForTests();
        sensor = new TestSensor("S01", "TestSensor", "lux", 100);
        device = new MockDevice("D01");
        sensor.addSlave(device);
    }

    @Test
    void simulateValue_shouldUpdateCurrentValue() {
        sensor.simulateValue(350);
        assertEquals(350, sensor.getCurrentValue());
    }

    @Test
    void notifySlaves_shouldTurnOnDevice_ifBelowThreshold() {
        device.turnOff();
        sensor.simulateValue(300); // below 400
        assertTrue(device.turnedOn);
    }

    @Test
    void notifySlaves_shouldTurnOffDevice_ifAboveThreshold() {
        device.turnOn();
        sensor.simulateValue(500); // above 600
        assertTrue(device.turnedOff);
    }

    @Test
    void toString_shouldContainSensorId_andUnit() {
        String output = sensor.toString();
        assertTrue(output.contains("TestSensor"), "toString output should contain sensor name");
        assertTrue(output.contains("lux"), "toString output should contain unit name");
    }

    @Test
    void setSensorName_shouldUpdateNameField() {
        TestSensor localSensor = new TestSensor("S99", "OldName", "lux", 50);
        localSensor.setSensorName("UpdatedName");

        assertEquals("UpdatedName", localSensor.getSensorName(), "Sensor name did not update correctly");
    }
}
