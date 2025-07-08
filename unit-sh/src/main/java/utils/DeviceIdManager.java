package utils;

import devices.Device;
import devices.DeviceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeviceIdManager {

    private static final DeviceIdManager instance = new DeviceIdManager();
    private final Set<String> assignedIds = new HashSet<>();

    private DeviceIdManager() {}

    public static DeviceIdManager getInstance() {
        return instance;
    }

    // ✅ Load known device IDs (typically from Excel at startup)
    public synchronized void setExistingDevices(List<Device> existingDevices) {
        assignedIds.clear();
        if (existingDevices != null) {
            existingDevices.forEach(device -> assignedIds.add(device.getId()));
        }
    }

    // ✅ Main generator method (custom handling for SMART_LIGHT)
    public synchronized String generateId(String deviceType) {
        String prefix = resolvePrefix(deviceType);

        int max = assignedIds.stream()
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        int next = max + 1;
        String newId = String.format("%s%03d", prefix, next);

        assignedIds.add(newId);
        return newId;
    }

    // ✅ Shortcut for enums
    public synchronized String generateIdForType(DeviceType type) {
        return generateId(type.name());
    }

    public synchronized boolean isIdTaken(String id) {
        return assignedIds.contains(id);
    }

    // 💡 Prefix overrides go here
    private String resolvePrefix(String deviceTypeName) {
        System.out.println("🔍 resolvePrefix called with: " + deviceTypeName);
        return switch (deviceTypeName.toUpperCase()) {
            case "SMART_LIGHT" -> "SL";
            case "WASHING_MACHINE" -> "WM";
            case "DRYER" -> "DR";
            case "THERMOSTAT" -> "TH";
            case "LIGHT" -> "LI";
            default -> deviceTypeName.substring(0, Math.min(2, deviceTypeName.length())).toUpperCase();
        };
    }
}
