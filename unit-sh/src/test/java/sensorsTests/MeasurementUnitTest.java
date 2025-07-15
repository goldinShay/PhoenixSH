package sensorsTests;

import org.junit.jupiter.api.Test;
import sensors.MeasurementUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MeasurementUnitTest {

    @Test
    void whenCallingToString_shouldReturnDisplayString() {
        assertEquals("lux", MeasurementUnit.LUX.toString());
        assertEquals("°C", MeasurementUnit.CELSIUS.toString());
        assertEquals("Pa", MeasurementUnit.PASCAL.toString());
        assertEquals("ON/OFF", MeasurementUnit.ON_OFF.toString());
    }

    @Test
    void whenCallingGetDisplay_shouldMatchToStringValue() {
        for (MeasurementUnit unit : MeasurementUnit.values()) {
            assertEquals(unit.toString(), unit.getDisplay());
        }
    }

    @Test
    void whenCallingFromString_shouldReturnCorrectEnum_basedOnDisplayName() {
        assertEquals(MeasurementUnit.LUX, MeasurementUnit.fromString("lux"));
        assertEquals(MeasurementUnit.CELSIUS, MeasurementUnit.fromString("°C"));
        assertEquals(MeasurementUnit.PASCAL, MeasurementUnit.fromString("pa"));
        assertEquals(MeasurementUnit.ON_OFF, MeasurementUnit.fromString("on/off"));
    }

    @Test
    void whenCallingFromString_shouldReturnCorrectEnum_basedOnEnumName() {
        assertEquals(MeasurementUnit.PERCENT, MeasurementUnit.fromString("PERCENT"));
        assertEquals(MeasurementUnit.LITERS, MeasurementUnit.fromString("liters"));
    }

    @Test
    void whenCallingFromString_withInvalidInput_shouldReturnUNKNOWN() {
        assertEquals(MeasurementUnit.UNKNOWN, MeasurementUnit.fromString("VIBRANCE"));
        assertEquals(MeasurementUnit.UNKNOWN, MeasurementUnit.fromString(""));
        assertEquals(MeasurementUnit.UNKNOWN, MeasurementUnit.fromString("°K"));
    }
}
