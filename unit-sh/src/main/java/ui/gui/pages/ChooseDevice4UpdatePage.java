package ui.gui.pages;

import devices.DeviceType;
import devices.actions.LiveDeviceState;
import storage.DeviceStorage;
import ui.gui.PageNavigator;
import ui.gui.devicesListPages.ChooseLightsUpdatePage;

import javax.swing.*;
import java.awt.*;

public class ChooseDevice4UpdatePage extends JPanel {
    public static final int PAGE_NUMBER = 112;

    public ChooseDevice4UpdatePage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // === Title ===
        JPanel displayPanel = new JPanel(new GridLayout(1, 1));
        displayPanel.setPreferredSize(new Dimension(800, 120));
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setBorder(BorderFactory.createTitledBorder(null,
                "Choose Device / Sensor", 0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel("📟 Select Category to Update", JLabel.CENTER);
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        displayPanel.add(titleLabel);

        // === Center Buttons ===
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        centerPanel.add(createCategoryButton("LIGHT", () -> {
            DeviceStorage.reloadFromExcel(); // 🔄 FIRST: Fresh data in memory
            DeviceStorage.getDevices().values().forEach(device -> {
                if (device.isOn()) {
                    LiveDeviceState.turnOn(device);
                } else {
                    LiveDeviceState.turnOff(device);
                }
            });
            ChooseLightsUpdatePage page = ChooseLightsUpdatePage.loadFresh(0, 120, DeviceType.LIGHT, DeviceType.SMART_LIGHT);            PageNavigator.registerPage(120, page);
            PageNavigator.goToPage(120);
        }));

        centerPanel.add(createCategoryButton("HOUSE UTILS", () -> {
            System.out.println("🛠️ HOUSE UTILS selected");
        }));

        centerPanel.add(createCategoryButton("SECURITY", () -> {
            System.out.println("🛠️ SECURITY selected");
        }));

        centerPanel.add(createCategoryButton("SENSOR", () -> {
            System.out.println("🛠️ SENSOR selected");
        }));

        // === Footer ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel("Page " + PAGE_NUMBER);
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtonPanel.setBackground(Color.BLACK);

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back");
        backBtn.addActionListener(e -> PageNavigator.goToPage(100));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        navButtonPanel.add(backBtn);
        navButtonPanel.add(homeBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navButtonPanel, BorderLayout.EAST);

        // === Final Assembly ===
        add(displayPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createCategoryButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(180, 180));
        button.addActionListener(e -> action.run());
        return button;
    }
}
