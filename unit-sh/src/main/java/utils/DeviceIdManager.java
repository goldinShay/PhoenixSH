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

    // ✅ Load known device IDs
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

    // ✅ Generate ID from DeviceType enum
    public synchronized String generateIdForType(DeviceType type) {
        String prefix = resolveDevicePrefix(type);
        return generateNextId(prefix);
    }

    // ✅ Generate ID for sensors via SensorType enum
    public synchronized String generateIdForSensorType(SensorType type) {
        String prefix = resolveSensorPrefix(type);
        return generateNextId(prefix);
    }

    // 🧠 Core ID generator based on prefix
    private String generateNextId(String prefix) {
        int max = assignedIds.stream()
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        String newId = String.format("%s%03d", prefix, max + 1);
        assignedIds.add(newId);
        return newId;
    }

    // 💬 For GUI uses that pass raw string types
    public synchronized String generateId(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException("❌ Device type name cannot be blank.");
        }

        String prefix = resolvePrefix(typeName.trim().toUpperCase());
        return generateNextId(prefix);
    }

    // 🔍 Prefix resolver for raw strings
    private String resolvePrefix(String typeName) {
        return switch (typeName) {
            case "SMART_LIGHT"     -> "SL";
            case "WASHING_MACHINE" -> "WM";
            case "DRYER"           -> "DR";
            case "THERMOSTAT"      -> "TH";
            case "LIGHT"           -> "LI";
            case "SENSOR"          -> "SN"; // Fallback, avoid overlap
            default                -> typeName.substring(0, Math.min(2, typeName.length()));
        };
    }

    private String resolveDevicePrefix(DeviceType type) {
        return switch (type) {
            case SMART_LIGHT     -> "SL";
            case WASHING_MACHINE -> "WM";
            case DRYER           -> "DR";
            case THERMOSTAT      -> "TH";
            case LIGHT           -> "LI";
            case SENSOR          -> "SN";
            default              -> type.name().substring(0, 2).toUpperCase();
        };
    }

    private String resolveSensorPrefix(SensorType type) {
        return switch (type) {
            case LIGHT            -> "LITs";
            case TEMPERATURE      -> "TMPs";
            case HUMIDITY         -> "HUMs";
            case MOTION           -> "MOTs";
            case SOFTENER_LEVEL   -> "WSLs";
            case DETERGENT_LEVEL,
                    WATER_LEVEL      -> "WDLs";
            default               -> type.name().substring(0, 3).toUpperCase() + "s";
        };
    }

    public synchronized boolean isIdTaken(String id) {
        return assignedIds.contains(id);
    }

    public synchronized void addKnownIds(Collection<String> ids) {
        ids.stream()
                .filter(Objects::nonNull)
                .forEach(assignedIds::add);
    }
    public synchronized String generateIdWithPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("❌ Prefix cannot be null or empty.");
        }
        System.out.println("🔗 ID generation requested with prefix: " + prefix);
        return generateNextId(prefix);
    }
    public static String getNextAvailableId(String prefix, Set<String> usedIds) {
        int counter = 1;
        String candidate;

        do {
            candidate = String.format("%s%03d", prefix, counter++);
        } while (usedIds.contains(candidate));

        return candidate;
    }

}
