package storage.xlc.sheetsCommand;

import java.util.EnumMap;
import java.util.Map;

public enum SensorSheetCommand {
    TYPE,
    ID,
    NAME,
    UNIT,
    CURRENT_VALUE,
    ADDED_TS,
    UPDATED_TS,
    REMOVED_TS;

    public String label() {
        return name(); // 👈 Could be refactored to user-friendly names if needed
    }
    public static Map<SensorSheetCommand, Integer> getColumnMap() {
        Map<SensorSheetCommand, Integer> map = new EnumMap<>(SensorSheetCommand.class);
        SensorSheetCommand[] values = SensorSheetCommand.values();
        for (int i = 0; i < values.length; i++) {
            map.put(values[i], i);
        }
        return map;
    }

}

