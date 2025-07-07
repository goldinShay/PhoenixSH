package ui.gui;

import javax.swing.*;
import java.awt.*;

public class WelcomeMenuPage extends JPanel {
    public static final int PAGE_NUMBER = 50;

    public WelcomeMenuPage() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Top simulated LCD
        JPanel lcdPanel = new JPanel(new GridLayout(3, 1));
        lcdPanel.setBackground(Color.BLACK);
        lcdPanel.setPreferredSize(new Dimension(800, 120));

        lcdPanel.add(buildLCDRow("Welcome to:", JLabel.CENTER, Color.LIGHT_GRAY));
        lcdPanel.add(buildLCDRow("PhoenixSH", JLabel.CENTER, Color.WHITE));
        lcdPanel.add(buildLCDRow("V - 1.0", JLabel.CENTER, Color.GRAY));

        // Center buttons
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

        // 💡 Hook b1 to DeviceSettingsPage
        b1.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.setContentPane(new DeviceSettingsPage());
            topFrame.revalidate();
            topFrame.repaint();
        });

        centerPanel.add(Box.createHorizontalGlue());
        centerPanel.add(b1);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(b2);
        centerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        centerPanel.add(b3);
        centerPanel.add(Box.createHorizontalGlue());

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page: %03d - Welcome Menu", PAGE_NUMBER));
        pageLabel.setForeground(Color.GREEN);

        JButton settingsBtn = new JButton("⚙️ Settings");
        settingsBtn.setFont(new Font("Arial", Font.PLAIN, 12));

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(settingsBtn, BorderLayout.EAST);

        // Layout
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
