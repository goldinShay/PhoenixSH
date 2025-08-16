package ui.gui.guiDeviceControl;

import devices.Device;
import storage.DeviceStorage;
import storage.xlc.XlDeviceManager;
import ui.gui.PageNavigator;
import utils.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class updateDeviceControlPage extends JPanel {
    public static final int PAGE_NUMBER = 215;
    private final Device device;

    public updateDeviceControlPage(Device device) {
        this.device = device;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(800, 100));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "Update Device",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel title = new JLabel("🔧 Editing: " + device.getName(), JLabel.CENTER);
        title.setForeground(Color.LIGHT_GRAY);
        title.setFont(new Font("Monospaced", Font.BOLD, 16));

        header.add(title, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BACKGROUND_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        Dimension fieldSize = new Dimension(300, 30);

        JTextField nameField = new JTextField(device.getName());
        nameField.setMaximumSize(fieldSize);

        List<String> brandOptions = Arrays.asList("Philips", "Bosch", "Siemens", "LG", "Samsung");
        JComboBox<String> brandDropdown = new JComboBox<>(brandOptions.toArray(new String[0]));
        brandDropdown.setSelectedItem(device.getBrand());
        brandDropdown.setMaximumSize(fieldSize);

        List<String> modelOptions = Arrays.asList("Model A", "Model B", "Model C", "Model D");
        JComboBox<String> modelDropdown = new JComboBox<>(modelOptions.toArray(new String[0]));
        modelDropdown.setSelectedItem(device.getModel());
        modelDropdown.setMaximumSize(fieldSize);

        JTextField thresholdField = new JTextField(String.valueOf(device.getAutoThreshold()));
        thresholdField.setMaximumSize(fieldSize);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton updateBtn = new JButton("Update Device");
        updateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        updateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        updateBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newBrand = (String) brandDropdown.getSelectedItem();
            String newModel = (String) modelDropdown.getSelectedItem();
            String thresholdText = thresholdField.getText().trim();

            if (newName.isBlank() || newBrand.isBlank() || newModel.isBlank() || thresholdText.isBlank()) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("⚠️ All fields must be filled.");
                return;
            }

            if (!newName.equalsIgnoreCase(device.getName())) {
                boolean nameExists = DeviceStorage.getDevices().values().stream()
                        .anyMatch(d -> d.getName().equalsIgnoreCase(newName));
                if (nameExists) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("❌ Name already exists: " + newName);
                    return;
                }
            }

            try {
                double newThreshold = Double.parseDouble(thresholdText);
                device.setName(newName);
                device.setBrand(newBrand);
                device.setModel(newModel);
                device.setAutoThreshold(newThreshold, true);

                try {
                    boolean success = XlDeviceManager.updateDevice(device);
                    if (success) {
                        statusLabel.setForeground(Color.GREEN);
                        statusLabel.setText("✅ Device updated successfully.");
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("❌ Failed to update device.");
                    }
                } catch (IOException ioEx) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("📄 Excel update failed: " + ioEx.getMessage());
                    ioEx.printStackTrace(); // Optional: for debugging
                }

            } catch (NumberFormatException ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("⚠️ Threshold must be a number.");
            }
        });

        form.add(new JLabel("Device Name:")).setForeground(Color.LIGHT_GRAY);
        form.add(nameField);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Brand:")).setForeground(Color.LIGHT_GRAY);
        form.add(brandDropdown);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Model:")).setForeground(Color.LIGHT_GRAY);
        form.add(modelDropdown);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Auto Threshold:")).setForeground(Color.LIGHT_GRAY);
        form.add(thresholdField);
        form.add(Box.createVerticalStrut(20));
        form.add(updateBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(statusLabel);

        return form;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel("Page " + PAGE_NUMBER);
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back");
        backBtn.addActionListener(e -> PageNavigator.goToPage(210));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setBackground(Color.BLACK);
        navPanel.add(backBtn);
        navPanel.add(homeBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navPanel, BorderLayout.EAST);

        return footer;
    }
}