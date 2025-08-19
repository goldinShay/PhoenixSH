package ui.gui.pages;

import devices.Device;
import ui.gui.utils.DeviceStatusUtils;
import storage.DeviceStorage;
import storage.xlc.XlDeviceManager;
import ui.gui.PageNavigator;
import ui.gui.managers.GuiAutoOpManager;

import javax.swing.*;
import java.awt.*;

public class AutoOpControlPage extends JPanel {

    private final int currentPageNumber = 300;

    public AutoOpControlPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // 🔹 Header
        add(createHeader(), BorderLayout.NORTH);

        // 🔹 Device List
        JPanel deviceList = new JPanel(new GridLayout(0, 1));
        deviceList.setBackground(Color.DARK_GRAY);

        for (Device storedDevice : DeviceStorage.getDevices().values()) {
            Device device = XlDeviceManager.getDeviceById(storedDevice.getId()); // ✅ Fresh state
            int pageId = 300 + Integer.parseInt(device.getId().replaceAll("[^0-9]", ""));

            JButton btn = new JButton("🔦 " + device.getName());
            btn.setFont(new Font("Monospaced", Font.PLAIN, 14));
            btn.setBackground(device.isAutomationEnabled() ? Color.GREEN : Color.LIGHT_GRAY);
            btn.setToolTipText("Configure AutoOp for " + device.getName());

            btn.addActionListener(e -> {
                JPanel wrappedPanel = new JPanel(new BorderLayout());
                wrappedPanel.setBackground(Color.DARK_GRAY);

                JPanel centerWrapper = new JPanel(new BorderLayout());
                centerWrapper.setBackground(Color.DARK_GRAY);
                centerWrapper.add(new GuiAutoOpManager(device), BorderLayout.CENTER);

                wrappedPanel.add(centerWrapper, BorderLayout.CENTER);
                wrappedPanel.add(createFooter(pageId), BorderLayout.SOUTH);

                PageNavigator.registerPage(pageId, wrappedPanel);
                PageNavigator.goToPage(pageId);
            });

            deviceList.add(btn);
        }

        JScrollPane scrollPane = new JScrollPane(deviceList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.DARK_GRAY);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // 🔹 Footer
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(800, 100));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "⚙️ AutoOp Control Center",
                0, 0, new Font("Monospaced", Font.BOLD, 16), Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel("🔗 Manage Automated Actions", JLabel.CENTER);
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 18));

        header.add(titleLabel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFooter() {
        return createFooter(currentPageNumber);
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
        backBtn.addActionListener(e -> PageNavigator.goToPage(50)); // Back to welcome or matrix

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
