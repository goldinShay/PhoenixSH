package storage;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sensors.MeasurementUnit;
import sensors.Sensor;
import sensors.SensorFactory;
import sensors.SensorType;
import storage.xlc.sheetsCommand.SensorSheetCommand;
import utils.ClockUtil;
import utils.Log;
import utils.XlUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static storage.xlc.XlWorkbookUtils.getFilePath;

public class SensorStorage {

    private static final Map<String, Sensor> sensors = new HashMap<>();
    private static final String SHEET_SENSORS = "Sensors";


    // 🔄 Add or replace a sensor
    public static void addSensor(String sensorId, Sensor sensor) {
        sensors.put(sensorId, sensor);
    }

    // ❌ Remove a sensor
    public static Sensor removeSensor(String sensorId) {
        return sensors.remove(sensorId);
    }

    // 🔍 Get a specific sensor
    public static Sensor getSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    // 🗺️ Get all sensors (modifiable)
    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    // 📦 Get unmodifiable view (for safe display)
    public static Map<String, Sensor> getUnmodifiableSensors() {
        return Collections.unmodifiableMap(sensors);
    }

    // 🧹 Clear everything (e.g., for testing or reset)
    public static void clear() {
        sensors.clear();
    }
    public static void loadSensorsFromExcel() {
        Map<String, Sensor> existingSensors = getSensors(); // existing map

        try (FileInputStream fis = new FileInputStream(getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_SENSORS);
            if (sheet == null) {
                Log.warn("⚠️ Sheet 'Sensors' not found.");
                return;
            }

            Map<SensorSheetCommand, Integer> columnMap = SensorSheetCommand.getColumnMap();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String typeStr  = XlUtils.getCellValue(row, columnMap.get(SensorSheetCommand.TYPE));
                    String id       = XlUtils.getCellValue(row, columnMap.get(SensorSheetCommand.ID));
                    String name     = XlUtils.getCellValue(row, columnMap.get(SensorSheetCommand.NAME));
                    String unitStr  = XlUtils.getCellValue(row, columnMap.get(SensorSheetCommand.UNIT));
                    String valueStr = XlUtils.getCellValue(row, columnMap.get(SensorSheetCommand.CURRENT_VALUE));

                    SensorType type = SensorType.valueOf(typeStr.trim().toUpperCase());
                    MeasurementUnit unit = MeasurementUnit.fromString(unitStr.trim());
                    double value = Double.parseDouble(valueStr);
                    Sensor existing = existingSensors.get(id);

                    if (existing != null) {
                        // 🧠 Update existing sensor
                        existing.setSensorName(name);
                        if (!existing.getSensorType().equals(type)) {
                            Log.warn("⚠️ Sensor type mismatch for ID " + id + ": existing=" + existing.getSensorType() + ", Excel=" + type);
                        }
                        existing.setUnit(unit);
                        existing.setCurrentValue(value);
                        Log.debug("🔄 Sensor updated: " + id);
                    } else {
                        // 🆕 Create new sensor
                        Clock clock = ClockUtil.getClock();
                        Sensor sensor = SensorFactory.createSensor(type, id, name, unit, value, clock);
                        existingSensors.put(id, sensor);
                        Log.debug("📥 Sensor loaded: " + id);
                    }

                } catch (Exception e) {
                    Log.warn("❌ Skipping invalid sensor row #" + row.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("❌ Failed to load sensors from Excel: " + e.getMessage());
        }
    }
    public static Collection<Sensor> getAll() {
        return sensors.values();
    }
    public static Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public static Map<String, Sensor> getAllSensors() {
        return sensors;
    }
}
