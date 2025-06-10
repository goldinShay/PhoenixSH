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

    // ✅ Set this ONCE after loading devices from Excel
    public synchronized void setExistingDevices(List<Device> existingDevices) {
        assignedIds.clear();
        if (existingDevices != null) {
            existingDevices.forEach(device -> assignedIds.add(device.getId())); // Persist IDs properly
        }
    }

    public synchronized String generateId(String deviceType) {
        String prefix = deviceType.substring(0, Math.min(2, deviceType.length())).toUpperCase();

        int max = assignedIds.stream()
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        int next = max + 1;
        String newId = String.format("%s%03d", prefix, next);

        assignedIds.add(newId); // ✅ Ensure ID is stored before returning
        return newId;
    }

    // ✅ Shortcut for enums
    public synchronized String generateIdForType(DeviceType type) {
        return generateId(type.name());
    }

    // ✅ Check if an ID is already assigned
    public synchronized boolean isIdTaken(String id) {
        return assignedIds.contains(id);
    }
}
