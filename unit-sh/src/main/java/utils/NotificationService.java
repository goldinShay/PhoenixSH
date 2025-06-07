package utils;

public class NotificationService {

    public void notify(String message) {
        System.out.println("[🔔 NOTIFICATION] " + message);
    }

    public void notify(String deviceId, String message) {
        System.out.println("[🔔 DEVICE " + deviceId + "] " + message);
    }

    public void notifyError(String message) {
        System.err.println("[❌ ERROR] " + message);
    }

    public void notifySuccess(String message) {
        System.out.println("[✅ SUCCESS] " + message);
    }

    public void notifyWarning(String message) {
        System.out.println("[⚠️ WARNING] " + message);
    }
}
