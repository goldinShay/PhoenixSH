package ui.gui.pages;

import devices.DeviceType;
import devices.actions.LiveDeviceState;
import storage.DeviceStorage;
import ui.gui.PageNavigator;
import ui.gui.devicesListPages.ChooseLightsUpdatePage;
import ui.gui.devicesListPages.ChooseUtilDevicePage;

import javax.swing.*;
import java.awt.*;

public class ChooseDeviceCtrlPage extends JPanel {
    public static final int PAGE_NUMBER = 200;

    public ChooseDeviceCtrlPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // === Top Display Area ===
        JPanel displayPanel = new JPanel();
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setPreferredSize(new Dimension(800, 120));
        displayPanel.setLayout(new GridLayout(1, 1));
        displayPanel.setBorder(BorderFactory.createTitledBorder(null, "Choose a Device",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel placeholder = new JLabel("📟 Device Category Selection", JLabel.CENTER);
        placeholder.setForeground(Color.LIGHT_GRAY);
        placeholder.setFont(new Font("Monospaced", Font.BOLD, 16));
        displayPanel.add(placeholder);

        // === Center Buttons ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        Dimension squareSize = new Dimension(180, 180);

        JButton lightsBtn = createMainButton("LIGHTS");
        lightsBtn.addActionListener(e -> {
            DeviceStorage.reloadFromExcel();
            ChooseLightsUpdatePage page = ChooseLightsUpdatePage.loadFresh(0, 120, DeviceType.LIGHT, DeviceType.SMART_LIGHT);
            PageNavigator.registerPage(120, page);
            DeviceStorage.reloadFromExcel(); // 🔁 Refresh memory
            PageNavigator.registerPage(120, ChooseLightsUpdatePage.loadFresh(0, 120, DeviceType.LIGHT, DeviceType.SMART_LIGHT));
            PageNavigator.goToPage(120);
        });

        JButton utilsBtn = createMainButton("<html><center>HOUSE<br>UTILS</center></html>");
        utilsBtn.addActionListener(e -> {
            DeviceStorage.reloadFromExcel(); // 🔄 Refresh device data

            DeviceStorage.getDevices().values().forEach(device -> {
                if (device.isOn()) {
                    LiveDeviceState.turnOn(device);
                } else {
                    LiveDeviceState.turnOff(device);
                }
            });

            ChooseUtilDevicePage page = ChooseUtilDevicePage.loadFresh(
                    0, 400,
                    DeviceType.THERMOSTAT,
                    DeviceType.WASHING_MACHINE,
                    DeviceType.DRYER
            );

            PageNavigator.registerPage(400, page); // 🧭 Register before navigating
            PageNavigator.goToPage(400);           // 🚀 Launch the matrix
        });


        JButton securityBtn = createMainButton("SECURITY");

        for (JButton btn : java.util.List.of(lightsBtn, utilsBtn, securityBtn)) {
            btn.setPreferredSize(squareSize);
            btn.setMinimumSize(squareSize);
            btn.setMaximumSize(squareSize);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setVerticalAlignment(SwingConstants.CENTER);
        }

        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.add(lightsBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(utilsBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(securityBtn);
        centerPanel.add(Box.createHorizontalGlue());

        // === Footer ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page %03d", PAGE_NUMBER));
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

// Match the visual depth by nesting the button in a panel
        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtonPanel.setBackground(Color.BLACK);

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        navButtonPanel.add(homeBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navButtonPanel, BorderLayout.EAST);


        // === Layout All ===
        add(displayPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createMainButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setFocusPainted(false);
        return button;
    }
}
