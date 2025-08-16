package devices.actions;

import java.util.*;

public enum DeviceAction {
    ON, OFF,
    START, STOP,
    TEMP_UP, TEMP_DOWN,
    VOLUME_UP, VOLUME_DOWN,
    CHANNEL_UP, CHANNEL_DOWN,
    STATUS;

    // ───────────────────── Mapping ─────────────────────

    private static final Map<String, List<DeviceAction>> deviceActionsMap = new HashMap<>();

    public static void loadDeviceActions(Map<String, List<String>> rawSheetData) {
        for (Map.Entry<String, List<String>> entry : rawSheetData.entrySet()) {
            String deviceId = entry.getKey();
            List<DeviceAction> parsedActions = new ArrayList<>();

            for (String raw : entry.getValue()) {
                try {
                    parsedActions.add(fromString(raw));
                } catch (IllegalArgumentException ignored) {
                    // Skip unknown actions silently
                }
            }

            deviceActionsMap.put(deviceId, parsedActions);
        }
    }

    public static List<DeviceAction> getActionsForDevice(String deviceId) {
        return deviceActionsMap.getOrDefault(deviceId, Collections.emptyList());
    }

    // ───────────────────── Parser ─────────────────────

    public static DeviceAction fromString(String input) {
        return switch (input.trim().toUpperCase()) {
            case "ON" -> ON;
            case "OFF" -> OFF;
            case "START" -> START;
            case "STOP" -> STOP;
            case "TEMP+" -> TEMP_UP;
            case "TEMP-" -> TEMP_DOWN;
            case "VOL+" -> VOLUME_UP;
            case "VOL-" -> VOLUME_DOWN;
            case "CH+" -> CHANNEL_UP;
            case "CH-" -> CHANNEL_DOWN;
            case "STATUS" -> STATUS;
            default -> throw new IllegalArgumentException("Unknown action: " + input);
        };
    }
}
