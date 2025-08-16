package storageTests;

import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import autoOp.AutoOpExcelReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoOpExcelReaderTest {

    @Test
    void whenSheetIsMissing_thenReturnsEmptyList() {
        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheet("Sense_Control")).thenReturn(null);

        List<AutoOpExcelReader.AutoOpRecord> result = AutoOpExcelReader.readLinks(workbook);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenSheetHasValidRows_thenParsesCorrectly() {
        Workbook workbook = mock(Workbook.class);
        Sheet sheet = mock(Sheet.class);
        Row row = mock(Row.class);

        Cell cell1 = mock(Cell.class); // Slave ID
        Cell cell3 = mock(Cell.class); // Sensor ID
        Cell cell4 = mock(Cell.class); // AutoOn
        Cell cell5 = mock(Cell.class); // AutoOff

        when(workbook.getSheet("Sense_Control")).thenReturn(sheet);
        when(sheet.iterator()).thenReturn(List.of(row).iterator());

        when(row.getRowNum()).thenReturn(1);
        when(row.getCell(1)).thenReturn(cell1);
        when(row.getCell(3)).thenReturn(cell3);
        when(row.getCell(4)).thenReturn(cell4);
        when(row.getCell(5)).thenReturn(cell5);

        when(cell1.getStringCellValue()).thenReturn("D123");
        when(cell3.getStringCellValue()).thenReturn("S789");
        when(cell4.getNumericCellValue()).thenReturn(10.5);
        when(cell5.getNumericCellValue()).thenReturn(5.5);

        List<AutoOpExcelReader.AutoOpRecord> result = AutoOpExcelReader.readLinks(workbook);
        assertEquals(1, result.size());

        AutoOpExcelReader.AutoOpRecord record = result.get(0);
        assertEquals("D123", record.linkedDeviceId());
        assertEquals("S789", record.sensorId());
        assertEquals(10.5, record.autoOn());
        assertEquals(5.5, record.autoOff());
    }
}
