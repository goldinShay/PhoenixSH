package ui.gui;

import javax.swing.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("PhoenixSH");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);

        // Set the first page as WelcomeMenuPage
        add(new WelcomeMenuPage());
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("✅ MainWindow: Launch triggered.");
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
