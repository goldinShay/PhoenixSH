package utils;

public class NotificationService {
    public void notify(String message) {
        System.out.println("[NOTIFICATION] " + message);
    }

    public void notify(String deviceId, String message) {
        System.out.println("[NOTIFICATION] devices.devcs.Device " + deviceId + ": " + message);
    }

}
