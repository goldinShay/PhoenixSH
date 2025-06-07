package utils;

import devices.DeviceType;
import java.util.HashMap;
import java.util.Map;

public class DeviceIdManager {

    private static final DeviceIdManager instance = new DeviceIdManager();
    private final Map<String, Integer> typeCounters = new HashMap<>();

    private DeviceIdManager() {}

    public static DeviceIdManager getInstance() {
        return instance;
    }

    public synchronized String generateId(String deviceType) {
        String prefix = deviceType.substring(0, Math.min(2, deviceType.length())).toUpperCase();
        int next = typeCounters.getOrDefault(prefix, 0) + 1;
        typeCounters.put(prefix, next);
        return String.format("%s%03d", prefix, next);
    }

    // ✅ This is the new friendly method using DeviceType
    public synchronized String generateIdForType(DeviceType type) {
        return generateId(type.name());
    }
}
