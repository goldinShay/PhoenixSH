package ui.gui.guiDeviceControl;

import devices.Device;
import devices.actions.LiveDeviceState;
import storage.xlc.XlDeviceManager;
import ui.gui.PageNavigator;
import ui.gui.managers.GuiStateManager;
import ui.gui.managers.GuiUtils;
import utils.Theme;

import javax.swing.*;
import java.awt.*;

public class LightControlPage extends JPanel {
    private Device device;
    private final int currentPageNumber;
    private JLabel statusLabel;

    public LightControlPage(Device device, int pageNumber) {
        this.device = XlDeviceManager.getDeviceById(device.getId()); // ✅ Always use fresh device state
        this.currentPageNumber = pageNumber;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        add(createHeader(), BorderLayout.NORTH);
        add(createControlPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(800, 100));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "Light Control Panel",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        String name = (device != null) ? device.getName() : "No Device";
        JLabel nameLabel = new JLabel("💡 " + name, JLabel.LEFT);
        nameLabel.setForeground(Color.LIGHT_GRAY);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        statusLabel = new JLabel(getStatusText(), JLabel.RIGHT);
        statusLabel.setForeground(getStatusColor());
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        JLabel autoOpLabel = new JLabel(device.isAutomationEnabled() ? "⚙️ AutoOp ON" : "⚙️ AutoOp OFF", JLabel.CENTER);
        autoOpLabel.setForeground(device.isAutomationEnabled() ? Color.GREEN : Color.RED);
        autoOpLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        header.add(nameLabel, BorderLayout.WEST);
        header.add(autoOpLabel, BorderLayout.CENTER);
        header.add(statusLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        controlPanel.setBackground(Theme.BACKGROUND_DARK);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        controlPanel.add(createActionButton("TURN ON", () -> {
            device.turnOn();
            GuiUtils.syncGuiAfterDeviceUpdate(device);
            refreshStatusLabel();
        }));

        controlPanel.add(createActionButton("TURN OFF", () -> {
            device.turnOff();
            GuiUtils.syncGuiAfterDeviceUpdate(device);
            refreshStatusLabel();
        }));

        controlPanel.add(createActionButton("SCHEDULER", () -> {
            System.out.println("🗓️ Scheduler clicked for " + device.getName());
            // PageNavigator.goToPage(...); // Add scheduler page if needed
        }));

        controlPanel.add(createActionButton("AutoOp", () -> {
            boolean enabled = !device.isAutomationEnabled();
            device.setAutomationEnabled(enabled);
            System.out.println("🔁 AutoOp " + (enabled ? "enabled" : "disabled") + " for " + device.getName());
            GuiStateManager.refreshDeviceControlPage(device); // ✅ Refresh page after toggle
        }));

        controlPanel.add(createActionButton("TEST LIGHT", () -> {
            device.testDevice();
        }));

        controlPanel.add(createActionButton("UPDATE DEVICE", () -> {
            PageNavigator.registerPage(215, new updateDeviceControlPage(device));
            PageNavigator.goToPage(215);
        }));

        return controlPanel;
    }

    private JButton createActionButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(Theme.BUTTON_GRAY);
        button.setForeground(Color.DARK_GRAY);
        button.setPreferredSize(new Dimension(200, 60));
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel("Page " + currentPageNumber);
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back");
        backBtn.addActionListener(e -> {
            GuiStateManager.refreshDeviceMatrix(); // ✅ Refresh matrix
            PageNavigator.goToPage(120);           // ✅ Navigate to matrix page
        });

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

    private void refreshStatusLabel() {
        this.device = XlDeviceManager.getDeviceById(device.getId()); // ✅ Refresh device state
        statusLabel.setText(getStatusText());
        statusLabel.setForeground(getStatusColor());
    }

    private String getStatusText() {
        return LiveDeviceState.isOn(device) ? "🟢 ON" : "🔴 OFF";
    }

    private Color getStatusColor() {
        return LiveDeviceState.isOn(device) ? Color.GREEN : Color.RED;
    }
}