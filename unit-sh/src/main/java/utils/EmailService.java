package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "465";
    private static final String USERNAME = "javagoldin@gmail.com";  // 🔄 Replace with your sender email
    private static final String PASSWORD = "nwmq-woir-psmf-tyqk";  // 🔄 Replace with your sender email's app password

    public static void sendDeviceActionEmail(String recipient, String deviceType, String deviceId, String deviceName, String action) {
        Properties props = new Properties();
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Device Action Notification - PhoenixSH");
            message.setText("🔔 Device Action Executed:\n\n" +
                    "📌 Type: " + deviceType + "\n" +
                    "📌 ID: " + deviceId + "\n" +
                    "📌 Name: " + deviceName + "\n" +
                    "🔄 Action: " + action);

            Transport.send(message);
            System.out.println("📧 Email sent successfully to " + recipient);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}
