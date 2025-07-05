package ui.gui;

import javax.swing.*;
import java.awt.*;

public class WelcomeMenuPage extends JPanel {
    public static final int PAGE_NUMBER = 50;

    public WelcomeMenuPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Top simulated LCD
        JPanel lcdPanel = new JPanel(new GridLayout(4, 1));
        lcdPanel.setBackground(Color.BLACK);
        lcdPanel.setPreferredSize(new Dimension(800, 120));

        lcdPanel.add(buildLCDRow("Main Menu", JLabel.CENTER, Color.GREEN));
        lcdPanel.add(buildLCDRow("Welcome to:", JLabel.CENTER, Color.LIGHT_GRAY));
        lcdPanel.add(buildLCDRow("PhoenixSH", JLabel.CENTER, Color.WHITE));
        lcdPanel.add(buildLCDRow("V - 1.0", JLabel.CENTER, Color.GRAY));

        // Center buttons
        // Center buttons (horizontal row with square size)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

// Create uniformly sized square buttons
        Dimension squareSize = new Dimension(120, 120);

        JButton b1 = createMainButton("Device Settings");
        JButton b2 = createMainButton("Device Control");
        JButton b3 = createMainButton("Scheduler");

        b1.setPreferredSize(squareSize);
        b2.setPreferredSize(squareSize);
        b3.setPreferredSize(squareSize);

// Padding between buttons
        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.add(b1);
        centerPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        centerPanel.add(b2);
        centerPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        centerPanel.add(b3);
        centerPanel.add(Box.createHorizontalGlue());

        // Footer panel
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel("Page: 050");
        pageLabel.setForeground(Color.GREEN);

        JButton settingsBtn = new JButton("⚙️ Settings");
        settingsBtn.setFont(new Font("Arial", Font.PLAIN, 12));

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(settingsBtn, BorderLayout.EAST);

        // Add everything
        add(lcdPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel buildLCDRow(String text, int alignment, Color color) {
        JLabel label = new JLabel(text, alignment);
        label.setForeground(color);
        label.setFont(new Font("Monospaced", Font.PLAIN, 16));
        label.setBackground(Color.BLACK);
        return label;
    }

    private JButton createMainButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setFocusPainted(false);
        return button;
    }
}
