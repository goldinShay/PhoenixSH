package storage.xlc.sheetsCommand;

import java.util.EnumMap;
import java.util.Map;


public enum DeviceSheetCommand {
    TYPE,
    DEVICE_ID,
    NAME,
    BRAND,
    MODEL,
    AUTO_ENABLED,
    AUTO_ON,
    AUTO_OFF,
    ACTIONS,
    STATE,
    ADDED_TS,
    UPDATED_TS,
    REMOVED_TS;

    public String label() {
        return switch (this) {
            case TYPE          -> "TYPE";
            case DEVICE_ID     -> "DEVICE_ID";
            case NAME          -> "NAME";
            case BRAND         -> "BRAND";
            case MODEL         -> "MODEL";
            case AUTO_ENABLED  -> "AUTO_ENABLED";
            case AUTO_ON       -> "AUTO_ON";
            case AUTO_OFF      -> "AUTO_OFF";
            case ACTIONS       -> "ACTIONS";
            case STATE         -> "STATE";
            case ADDED_TS      -> "ADDED_TS";
            case UPDATED_TS    -> "UPDATED_TS";
            case REMOVED_TS    -> "REMOVED_TS";
        };
    }
    public static Map<DeviceSheetCommand, Integer> getColumnMap() {
        Map<DeviceSheetCommand, Integer> map = new EnumMap<>(DeviceSheetCommand.class);
        DeviceSheetCommand[] values = DeviceSheetCommand.values();
        for (int i = 0; i < values.length; i++) {
            map.put(values[i], i);
        }
        return map;
    }
}
