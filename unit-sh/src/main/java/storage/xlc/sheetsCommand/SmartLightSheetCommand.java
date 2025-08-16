package storage.xlc.sheetsCommand;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum SmartLightSheetCommand {
    TYPE("Type"),
    DEVICE_ID("DeviceID"),
    NAME("Name"),
    BRAND("Brand"),
    MODEL("Model"),
    AUTO_ENABLED ("AutoEnabled"),
    AUTO_ON ("AUTO_ON"),
    COLOUR_MODE("ColourMode"),
    RED("Red"),
    GREEN("Green"),
    BLUE("Blue"),
    FX_MODE("FXMode"),
    LAST_UPDATED("LastUpdated");

    private final String label;

    SmartLightSheetCommand(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    // 🗂️ Create a map: SmartLightSheetCommand → column index
    public static Map<SmartLightSheetCommand, Integer> getColumnMap() {
        Map<SmartLightSheetCommand, Integer> map = new EnumMap<>(SmartLightSheetCommand.class);
        int col = 0;
        for (SmartLightSheetCommand cmd : values()) {
            map.put(cmd, col++);
        }
        return map;
    }
    public static Map<SmartLightSheetCommand, Integer> getDevicesColumnMap() {
        Map<SmartLightSheetCommand, Integer> map = new HashMap<>();
        int col = 0;
        for (SmartLightSheetCommand cmd : SmartLightSheetCommand.values()) {
            map.put(cmd, col++);
        }
        return map;
    }
}