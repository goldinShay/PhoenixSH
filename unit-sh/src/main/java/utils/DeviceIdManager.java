package utils;

import devices.Device;
import devices.DeviceType;
import sensors.SensorType;

import java.util.*;

public class DeviceIdManager {

    private static final DeviceIdManager instance = new DeviceIdManager();
    private final Set<String> assignedIds = new HashSet<>();

    private DeviceIdManager() {}


    public static DeviceIdManager getInstance() {
        return instance;
    }

    // ✅ Load known device IDs (typically from Excel at startup)
    public synchronized void setExistingDevices(List<Device> existingDevices) {
        if (existingDevices == null || existingDevices.isEmpty()) {
            Log.warn("⚠️ No existing devices loaded. ID manager will retain current state.");
            return;
        }
        
        existingDevices.stream()
                .map(Device::getId)
                .filter(Objects::nonNull)
                .forEach(assignedIds::add);

        Log.debug("📦 Existing device IDs updated → Count: " + assignedIds.size());
        Log.debug("🧾 Assigned IDs snapshot: " + assignedIds);
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
    public synchronized String generateIdForSensorType(SensorType sensorType) {
        String prefix = resolveSensorPrefix(sensorType);

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
    private String resolveSensorPrefix(SensorType type) {
        return switch (type) {
            case LIGHT -> "LITs";
            case TEMPERATURE -> "TMPs";
            case HUMIDITY -> "HUMs";
            case MOTION -> "MOTs";
            case SOFTENER_LEVEL -> "WSLs";
            case DETERGENT_LEVEL, WATER_LEVEL -> "WDLs";
            // Add more mappings as needed
            default -> type.name().substring(0, 3).toUpperCase() + "s";
        };
    }
    public synchronized void addKnownIds(Collection<String> ids) {
        ids.stream()
                .filter(Objects::nonNull)
                .forEach(assignedIds::add);
    }


}
