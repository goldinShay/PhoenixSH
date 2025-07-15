package ui.gui;

import devices.DeviceType;
import sensors.SensorType;
import storage.DeviceRegistry;
import storage.DeviceStorage;
import ui.gui.devicesListPages.ChooseLightToUpdatePage;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class AddDeviceMenuPage extends JPanel {
    public static final int PAGE_NUMBER = 110;

    public AddDeviceMenuPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // === Top Display (LCD-style) ===
        JPanel displayPanel = new JPanel();
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setPreferredSize(new Dimension(800, 120));
        displayPanel.setLayout(new GridLayout(1, 1));
        displayPanel.setBorder(BorderFactory.createTitledBorder(null, "Add Device / Sensor",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel header = new JLabel("📟 Register New Device into Smart System", JLabel.CENTER);
        header.setForeground(Color.LIGHT_GRAY);
        header.setFont(new Font("Monospaced", Font.BOLD, 16));
        displayPanel.add(header);

        // === Center Form ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        JLabel promptLabel = new JLabel("Choose a Device Type:");
        promptLabel.setForeground(Color.LIGHT_GRAY);
        promptLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JComboBox<DeviceType> deviceTypeCombo = new JComboBox<>(DeviceType.values());
        deviceTypeCombo.setMaximumSize(new Dimension(300, 30));

        JComboBox<SensorType> sensorTypeCombo = new JComboBox<>(SensorType.values());
        sensorTypeCombo.setMaximumSize(new Dimension(300, 30));
        sensorTypeCombo.setVisible(false); // Show only if SENSOR is selected

        deviceTypeCombo.addActionListener(e -> {
            DeviceType selected = (DeviceType) deviceTypeCombo.getSelectedItem();
            sensorTypeCombo.setVisible(selected == DeviceType.SENSOR);
            revalidate(); repaint();
        });

        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 30));
        nameField.setToolTipText("Device name (must be unique)");

        JTextField brandField = new JTextField();
        brandField.setMaximumSize(new Dimension(300, 30));
        brandField.setToolTipText("Brand");

        JTextField modelField = new JTextField();
        modelField.setMaximumSize(new Dimension(300, 30));
        modelField.setToolTipText("Model");

        JButton submitBtn = new JButton("Submit");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            DeviceType type = (DeviceType) deviceTypeCombo.getSelectedItem();
            Optional<SensorType> subtype = sensorTypeCombo.isVisible()
                    ? Optional.of((SensorType) sensorTypeCombo.getSelectedItem())
                    : Optional.empty();

            String name = nameField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();

            // Validate name
            if (name.isBlank()) {
                statusLabel.setText("⚠️ Please enter a valid name.");
                return;
            }

            if (DeviceRegistry.isNameTaken(name)) {
                statusLabel.setText("❌ Name already exists: " + name);
                return;
            }

            // Log selected values
            System.out.println("📋 DeviceType Selected: " + type);
            System.out.println("📋 SensorType Selected: " + subtype.orElse(null));
            System.out.println("📋 Device Name: " + name);

            boolean success = DeviceRegistry.registerDevice(type, subtype, name, brand, model);

            if (success) {
                statusLabel.setText("✅ Device registered!");

                // Refresh device list and go to updated matrix page
                DeviceStorage.reloadFromExcel();
                PageNavigator.registerPage(120, new ChooseLightToUpdatePage(0));
                PageNavigator.goToPage(120);
            } else {
                statusLabel.setText("❌ Registration failed.");
            }
        });
        ;

        centerPanel.add(promptLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(deviceTypeCombo);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(sensorTypeCombo);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(new JLabel("Device Name:")).setForeground(Color.LIGHT_GRAY);
        centerPanel.add(nameField);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(new JLabel("Brand:")).setForeground(Color.LIGHT_GRAY);
        centerPanel.add(brandField);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(new JLabel("Model:")).setForeground(Color.LIGHT_GRAY);
        centerPanel.add(modelField);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(submitBtn);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(statusLabel);

        // === Footer ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page %03d", PAGE_NUMBER));
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        footer.add(pageLabel, BorderLayout.WEST);
        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtonPanel.setBackground(Color.BLACK);

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back to previous page");
        backBtn.addActionListener(e -> PageNavigator.goToPage(100));

        navButtonPanel.add(backBtn);
        navButtonPanel.add(homeBtn);

        footer.add(navButtonPanel, BorderLayout.EAST);


        // === Assemble Layout ===
        add(displayPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}
