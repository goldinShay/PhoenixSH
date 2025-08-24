package ui.gui.guiDeviceControl;

import devices.Device;
import devices.Thermostat;
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

public class HeaterControlPage extends JPanel {
    private Thermostat thermostat;
    private final int currentPageNumber;

    private JLabel statusLabel;
    private JLabel autoOpLabel;
    private JLabel tempLabel;

    public HeaterControlPage(Device device, int pageNumber) {
        this.thermostat = (Thermostat) XlDeviceManager.getDeviceById(device.getId());
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
        header.setBorder(BorderFactory.createTitledBorder(null, "Heater Control Panel",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel("🔥 " + thermostat.getName(), JLabel.LEFT);
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel buttonGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        buttonGrid.setBackground(Theme.BACKGROUND_DARK);

        buttonGrid.add(createActionButton("TURN ON", () -> {
            thermostat.turnOn();
            GuiUtils.syncGuiAfterDeviceUpdate(thermostat);
            refreshStatusLabel();
        }));

        buttonGrid.add(createActionButton("TURN OFF", () -> {
            thermostat.turnOff();
            GuiUtils.syncGuiAfterDeviceUpdate(thermostat);
            refreshStatusLabel();
        }));

        buttonGrid.add(createActionButton("AutoOp", () -> {
            int pageId = 300 + Integer.parseInt(thermostat.getId().replaceAll("[^0-9]", ""));
            JPanel wrappedPanel = new JPanel(new BorderLayout());
            wrappedPanel.setBackground(Theme.BACKGROUND_DARK);

            JPanel centerWrapper = new JPanel(new BorderLayout());
            centerWrapper.setBackground(Theme.BACKGROUND_DARK);
            centerWrapper.add(new GuiAutoOpManager(thermostat), BorderLayout.CENTER);

            wrappedPanel.add(centerWrapper, BorderLayout.CENTER);
            wrappedPanel.add(createFooter(pageId), BorderLayout.SOUTH);

            if (!PageNavigator.isPageRegistered(pageId)) {
                PageNavigator.registerPage(pageId, wrappedPanel);
            }
            PageNavigator.goToPage(pageId);

            refreshAutoOpLabel();
        }));

        buttonGrid.add(createActionButton("TEST DEVICE", () -> {
            boolean success = DeviceTestService.testDeviceById(thermostat.getId());
            if (!success) {
                JOptionPane.showMessageDialog(null, "Device is ON or not found. Turn it off before testing.");
            }
        }));

        buttonGrid.add(createActionButton("UPDATE DEVICE", () -> {
            PageNavigator.registerPage(215, new updateDeviceControlPage(thermostat));
            PageNavigator.goToPage(215);
        }));

        // Temperature slider
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBackground(Theme.BACKGROUND_DARK);

        tempLabel = new JLabel("User Temp: " + thermostat.getUserTemp() + "°C", JLabel.CENTER);
        tempLabel.setForeground(Color.CYAN);
        tempLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JSlider tempSlider = new JSlider(16, 42, (int) thermostat.getUserTemp());
        tempSlider.setMajorTickSpacing(2);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        tempSlider.setBackground(Theme.BACKGROUND_DARK);
        tempSlider.setForeground(Color.LIGHT_GRAY);

        tempSlider.addChangeListener(e -> {
            thermostat.setUserTemp(tempSlider.getValue());
            GuiUtils.syncGuiAfterDeviceUpdate(thermostat);
            tempLabel.setText("User Temp: " + thermostat.getUserTemp() + "°C");
        });

        sliderPanel.add(tempLabel, BorderLayout.NORTH);
        sliderPanel.add(tempSlider, BorderLayout.CENTER);

        panel.add(buttonGrid, BorderLayout.NORTH);
        panel.add(sliderPanel, BorderLayout.CENTER);

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
            PageNavigator.goToPage(400);
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

    // 🔄 Label refreshers
    public void refreshStatusLabel() {
        this.thermostat = (Thermostat) XlDeviceManager.getDeviceById(thermostat.getId());
        statusLabel.setText(getStatusText());
        statusLabel.setForeground(getStatusColor());
    }

    public void refreshAutoOpLabel() {
        this.thermostat = (Thermostat) XlDeviceManager.getDeviceById(thermostat.getId());
        autoOpLabel.setText(getAutoOpText());
        autoOpLabel.setForeground(getAutoOpColor());
    }

    // 🔍 Label helpers
    private String getStatusText() {
        return LiveDeviceState.isOn(thermostat) ? "🟢 ON" : "🔴 OFF";
    }

    private Color getStatusColor() {
        return LiveDeviceState.isOn(thermostat) ? Color.GREEN : Color.RED;
    }

    private String getAutoOpText() {
        return thermostat.isAutomationEnabled() ? "⚙️ AutoOp ON" : "⚙️ AutoOp OFF";
    }

    private Color getAutoOpColor() {
        return thermostat.isAutomationEnabled() ? Color.GREEN : Color.RED;
    }
}
