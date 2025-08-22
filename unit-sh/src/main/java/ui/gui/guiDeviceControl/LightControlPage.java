package ui.gui.guiDeviceControl;

import devices.Device;
import devices.actions.LiveDeviceState;
import services.DeviceTestService;
import storage.xlc.XlDeviceManager;
import ui.gui.PageNavigator;
import ui.gui.managers.GuiAutoOpManager;
import ui.gui.managers.GuiStateManager;
import ui.gui.managers.GuiUtils;
import utils.Theme;

import javax.swing.*;
import java.awt.*;

public class LightControlPage extends JPanel {
    private Device device;
    private final int currentPageNumber;

    private JLabel statusLabel;
    private JLabel autoOpLabel;

    public LightControlPage(Device device, int pageNumber) {
        this.device = XlDeviceManager.getDeviceById(device.getId());
        this.currentPageNumber = pageNumber;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        add(createHeader(), BorderLayout.NORTH);
        add(createControlPanel(), BorderLayout.CENTER);
        add(createFooter(currentPageNumber), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(800, 100));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "Light Control Panel",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel("💡 " + device.getName(), JLabel.LEFT);
        nameLabel.setForeground(Color.LIGHT_GRAY);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        statusLabel = new JLabel(getStatusText(), JLabel.RIGHT);
        statusLabel.setForeground(getStatusColor());
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        autoOpLabel = new JLabel(getAutoOpText(), JLabel.CENTER);
        autoOpLabel.setForeground(getAutoOpColor());
        autoOpLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        header.add(nameLabel, BorderLayout.WEST);
        header.add(autoOpLabel, BorderLayout.CENTER);
        header.add(statusLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setBackground(Theme.BACKGROUND_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        panel.add(createActionButton("TURN ON", () -> {
            device.turnOn();
            GuiUtils.syncGuiAfterDeviceUpdate(device);
            refreshStatusLabel();
        }));

        panel.add(createActionButton("TURN OFF", () -> {
            device.turnOff();
            GuiUtils.syncGuiAfterDeviceUpdate(device);
            refreshStatusLabel();
        }));

        panel.add(createActionButton("SCHEDULER", () ->
                System.out.println("🗓️ Scheduler clicked for " + device.getName())));

        panel.add(createActionButton("AutoOp", () -> {
            System.out.println("🔁 AutoOp panel requested for " + device.getName());
            int pageId = 300 + Integer.parseInt(device.getId().replaceAll("[^0-9]", ""));

            JPanel wrappedPanel = new JPanel(new BorderLayout());
            wrappedPanel.setBackground(Theme.BACKGROUND_DARK);

            JPanel centerWrapper = new JPanel(new BorderLayout());
            centerWrapper.setBackground(Theme.BACKGROUND_DARK);
            centerWrapper.add(new GuiAutoOpManager(device), BorderLayout.CENTER);

            wrappedPanel.add(centerWrapper, BorderLayout.CENTER);
            wrappedPanel.add(createFooter(pageId), BorderLayout.SOUTH);

            if (!PageNavigator.isPageRegistered(pageId)) {
                PageNavigator.registerPage(pageId, wrappedPanel);
            }
            PageNavigator.goToPage(pageId);

            refreshAutoOpLabel(); // ✅ Only update AutoOp label
        }));

        panel.add(createActionButton("TEST LIGHT", () -> {
            boolean success = DeviceTestService.testDeviceById(device.getId());
            if (!success) {
                JOptionPane.showMessageDialog(null, "Device is ON or not found. Turn it off before testing.");
            }
        }));

        panel.add(createActionButton("UPDATE DEVICE", () -> {
            PageNavigator.registerPage(215, new updateDeviceControlPage(device));
            PageNavigator.goToPage(215);
        }));

        return panel;
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

    private JPanel createFooter(int pageId) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel("Page " + pageId);
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back");
        backBtn.addActionListener(e -> {
            GuiStateManager.refreshDeviceMatrix();
            PageNavigator.goToPage(120);
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

    // 🔄 Independent label refreshers
    public void refreshStatusLabel() {
        this.device = XlDeviceManager.getDeviceById(device.getId());
        statusLabel.setText(getStatusText());
        statusLabel.setForeground(getStatusColor());
    }

    public void refreshAutoOpLabel() {
        this.device = XlDeviceManager.getDeviceById(device.getId());
        autoOpLabel.setText(getAutoOpText());
        autoOpLabel.setForeground(getAutoOpColor());
    }

    // 🔍 Label text + color helpers
    private String getStatusText() {
        return LiveDeviceState.isOn(device) ? "🟢 ON" : "🔴 OFF";
    }

    private Color getStatusColor() {
        return LiveDeviceState.isOn(device) ? Color.GREEN : Color.RED;
    }

    private String getAutoOpText() {
        return device.isAutomationEnabled() ? "⚙️ AutoOp ON" : "⚙️ AutoOp OFF";
    }

    private Color getAutoOpColor() {
        return device.isAutomationEnabled() ? Color.GREEN : Color.RED;
    }
}