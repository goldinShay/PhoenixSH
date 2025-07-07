package ui.gui;

import javax.swing.*;
import java.awt.*;
import ui.gui.DeviceSettingsPage;


public class DeviceSettingsPage extends JPanel {
    public static final int PAGE_NUMBER = 100;

    public DeviceSettingsPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // === Top Display Area ===
        JPanel displayPanel = new JPanel();
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setPreferredSize(new Dimension(800, 120));
        displayPanel.setLayout(new GridLayout(1, 1));
        displayPanel.setBorder(BorderFactory.createTitledBorder(null, "Device Display",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel placeholder = new JLabel("📟 Device summary display...", JLabel.CENTER);
        placeholder.setForeground(Color.GREEN);
        placeholder.setFont(new Font("Monospaced", Font.BOLD, 16));
        displayPanel.add(placeholder);

        // === Center Buttons ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        Dimension squareSize = new Dimension(180, 180);

        JButton addBtn = createMainButton("<html><center>ADD<br>Device</center></html>");
        JButton updateBtn = createMainButton("<html><center>UPDATE<br>Device</center></html>");
        JButton removeBtn = createMainButton("<html><center>REMOVE<br>DEVICE</center></html>");

        for (JButton btn : java.util.List.of(addBtn, updateBtn, removeBtn)) {
            btn.setPreferredSize(squareSize);
            btn.setMinimumSize(squareSize);
            btn.setMaximumSize(squareSize);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setVerticalAlignment(SwingConstants.CENTER);
        }

        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.add(addBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(updateBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(removeBtn);
        centerPanel.add(Box.createHorizontalGlue());

        // === Footer ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page: %03d - Device Settings", PAGE_NUMBER));
        pageLabel.setForeground(Color.GREEN);

        JButton homeBtn = new JButton("🏠 Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        // TODO: add action listener to switch page here

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(homeBtn, BorderLayout.EAST);

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
