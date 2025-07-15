package ui.gui.devicesListPages;

import devices.Device;
import devices.DeviceType;
import storage.DeviceStorage;
import ui.gui.PageNavigator;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ChooseLightToUpdatePage extends JPanel {
    private static final int PAGE_SIZE = 10;
    private final int pageIndex;

    public ChooseLightToUpdatePage(int pageIndex) {
        this.pageIndex = pageIndex;

        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // === Title Panel ===
        JPanel displayPanel = new JPanel();
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setPreferredSize(new Dimension(800, 120));
        displayPanel.setLayout(new GridLayout(1, 1));
        displayPanel.setBorder(BorderFactory.createTitledBorder(null, "Choose Light Device",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel("📟 Select Light to Update", JLabel.CENTER);
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        displayPanel.add(titleLabel);

        // === Device Grid ===
        JPanel gridPanel = new JPanel(new GridLayout(4, 3, 20, 20));
        gridPanel.setBackground(Color.DARK_GRAY);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // 🔍 Get relevant lights
        DeviceStorage.reloadFromExcel(); // <- Ensure latest data is pulled
        List<Device> allLights = DeviceStorage.getDevices().values().stream()
                .filter(d -> d.getType() == DeviceType.LIGHT)
                .sorted(Comparator.comparing(Device::getId))
                .collect(Collectors.toList());


        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allLights.size());
        List<Device> pageDevices = new ArrayList<>();

        for (int i = start; i < end; i++) {
            pageDevices.add(allLights.get(i));
        }
        while (pageDevices.size() < PAGE_SIZE) pageDevices.add(null);

        // 🔘 Add device buttons
        for (int i = 0; i < 9; i++) {
            gridPanel.add(createNameButton(pageDevices.get(i), i));
        }

        // 🔘 Bottom row: nav + final device
        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            if (pageIndex > 0) {
                int prevId = 120 + (pageIndex - 1);
                PageNavigator.registerPage(prevId, new ChooseLightToUpdatePage(pageIndex - 1));
                PageNavigator.goToPage(prevId);
            }
        });

        gridPanel.add(backBtn);
        gridPanel.add(createNameButton(pageDevices.get(9), 9));

        JButton nextBtn = new JButton("→");
        nextBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        nextBtn.addActionListener(e -> {
            int totalPages = (int) Math.ceil(allLights.size() / (double) PAGE_SIZE);
            if (pageIndex + 1 < totalPages) {
                int nextId = 120 + (pageIndex + 1);
                PageNavigator.registerPage(nextId, new ChooseLightToUpdatePage(pageIndex + 1));
                PageNavigator.goToPage(nextId);
            } else {
                PageNavigator.goToPage(125); // ➡ SmartLights start here
            }
        });

        gridPanel.add(nextBtn);

        // === Footer ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page %03d", 120 + pageIndex));
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtons.setBackground(Color.BLACK);

        JButton prevBranchBtn = new JButton("←");
        prevBranchBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        prevBranchBtn.addActionListener(e -> PageNavigator.goToPage(112));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        navButtons.add(prevBranchBtn);
        navButtons.add(homeBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navButtons, BorderLayout.EAST);

        // === Final Assembly ===
        add(displayPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createNameButton(Device device, int position) {
        JButton button = new JButton();
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 180));

        if (device == null) {
            button.setText("NULL");
            button.setEnabled(false);
        } else {
            button.setText("<html><center>" + device.getName() + "</center></html>");
            button.addActionListener(e -> {
                System.out.println("🔧 Selected for update: " + device.getName());
                // PageNavigator.goToPage(new UpdateDeviceDetailsPage(device)); // Future hook
            });
        }

        return button;
    }
}
