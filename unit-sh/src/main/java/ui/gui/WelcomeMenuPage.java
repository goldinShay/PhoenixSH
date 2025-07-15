package ui.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WelcomeMenuPage extends JPanel {
    public static final int PAGE_NUMBER = 50;

    public WelcomeMenuPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // === Top Display Area (LCD-style) ===
        JPanel displayPanel = new JPanel();
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setPreferredSize(new Dimension(800, 120));
        displayPanel.setLayout(null); // Absolute layout for custom placement
        displayPanel.setBorder(BorderFactory.createTitledBorder(null, "Welcome Menu",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        // 🕓 Timestamp at top-left
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy_HH:mm");
        String timestamp = formatter.format(LocalDateTime.now());

        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        timeLabel.setBounds(10, 12, 200, 20); // Position near top-left
        displayPanel.add(timeLabel);

        // 📟 Main label centered
        JLabel titleLabel = new JLabel("📟 Welcome to PhoenixSH — V 1.0", JLabel.CENTER);
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        titleLabel.setBounds(0, 40, 800, 40); // Horizontally centered
        displayPanel.add(titleLabel);

        // === Center Control Buttons ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        Dimension squareSize = new Dimension(180, 180);

        JButton b1 = createMainButton("<html><center>Device<br>Settings</center></html>");
        JButton b2 = createMainButton("<html><center>Device<br>Control</center></html>");
        JButton b3 = createMainButton("Scheduler");

        for (JButton btn : java.util.List.of(b1, b2, b3)) {
            btn.setPreferredSize(squareSize);
            btn.setMinimumSize(squareSize);
            btn.setMaximumSize(squareSize);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setVerticalAlignment(SwingConstants.CENTER);
        }

        b1.addActionListener(e -> PageNavigator.goToPage(100));
        b2.addActionListener(e -> PageNavigator.goToPage(200));

        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.add(b1);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(b2);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(b3);
        centerPanel.add(Box.createHorizontalGlue());

        // === Footer ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page %03d", PAGE_NUMBER));
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtonPanel.setBackground(Color.BLACK);

        JButton settingsBtn = new JButton("Setup");
        settingsBtn.setFont(new Font("Arial", Font.PLAIN, 12));
// You can add a navigation action here if needed
// settingsBtn.addActionListener(e -> PageNavigator.goToPage(X));

        navButtonPanel.add(settingsBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navButtonPanel, BorderLayout.EAST);


        // === Final Assembly ===
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
