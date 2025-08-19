package ui.gui.managers;

import autoOp.AutoOpLinker;
import autoOp.AutoOpUnlinker;
import devices.Device;
import sensors.Sensor;
import storage.SensorStorage;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GuiAutoOpManager extends JPanel {

    private final Device device;
    private JComboBox<Sensor> sensorDropdown;

    public GuiAutoOpManager(Device device) {
        this.device = device;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
//        add(createFooter(), BorderLayout.SOUTH);
    }

    // ✅ Reuse header from LightControlPage
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(800, 100));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "AutoOp Control Panel",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        String name = (device != null) ? device.getName() : "No Device";
        JLabel nameLabel = new JLabel("💡 " + name, JLabel.LEFT);
        nameLabel.setForeground(Color.LIGHT_GRAY);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

//        JLabel statusLabel = new JLabel(getStatusText(), JLabel.RIGHT);
//        statusLabel.setForeground(getStatusColor());
//        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
//
//        JLabel autoOpLabel = new JLabel(device.isAutomationEnabled() ? "⚙️ AutoOp ON" : "⚙️ AutoOp OFF", JLabel.CENTER);
//        autoOpLabel.setForeground(device.isAutomationEnabled() ? Color.GREEN : Color.RED);
//        autoOpLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        header.add(nameLabel, BorderLayout.WEST);
//        header.add(autoOpLabel, BorderLayout.CENTER);
//        header.add(statusLabel, BorderLayout.EAST);

        return header;
    }

//    private String getStatusText() {
//        return device.getState().toString();
//    }
//
//    private Color getStatusColor() {
//        String state = device.getState();
//        if ("ON".equalsIgnoreCase(state)) {
//            return Color.GREEN;
//        } else if ("OFF".equalsIgnoreCase(state)) {
//            return Color.RED;
//        } else {
//            return Color.GRAY;
//        }
//    }

    // ✅ Sensor dropdown + buttons
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.DARK_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel sensorLabel = new JLabel("📡 Select Sensor:");
        sensorLabel.setForeground(Color.WHITE);
        sensorLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

        sensorDropdown = new JComboBox<>();
        populateSensorDropdown();

        JButton linkButton = new JButton("🔗 Link Sensor");
        linkButton.addActionListener(e -> linkSensor());
        device.setAutomationEnabled(true); // ✅ Only enable after link

        JButton unlinkButton = new JButton("❌ Unlink Sensor");
        unlinkButton.addActionListener(e -> unlinkSensor());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(sensorLabel, gbc);

        gbc.gridx = 1;
        panel.add(sensorDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(linkButton, gbc);

        gbc.gridx = 1;
        panel.add(unlinkButton, gbc);

        return panel;
    }

    private void populateSensorDropdown() {
        Map<String, Sensor> sensors = SensorStorage.getSensors();
        if (sensors != null) {
            for (Sensor sensor : sensors.values()) {
                sensorDropdown.addItem(sensor);
            }
        }
    }

    // ✅ Link logic
    private void linkSensor() {
        Sensor selectedSensor = (Sensor) sensorDropdown.getSelectedItem();
        if (selectedSensor == null) {
            appendFeedback("⚠️ No sensor selected.");
            return;
        }

        boolean success = AutoOpLinker.linkDeviceToSensor(device, selectedSensor);
        if (success) {
            device.setAutomationEnabled(true);
            device.setAutomationSensorId(selectedSensor.getSensorId()); // ✅ Add this line
            appendFeedback("✅ Linked " + device.getName() + " to " + selectedSensor.getSensorName());
        }
        else {
            appendFeedback("❌ Failed to link device.");
        }
    }

    // ✅ Unlink logic
    private void unlinkSensor() {
        AutoOpUnlinker.disable(device);
        appendFeedback("🧻 Unlinked device from sensor.");
    }

    private void appendFeedback(String message) {
        JOptionPane.showMessageDialog(this, message, "AutoOp Feedback", JOptionPane.INFORMATION_MESSAGE);
    }
}

