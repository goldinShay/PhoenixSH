package ui.gui;

import storage.xlc.XlWorkbookUtils;
import ui.gui.devicesListPages.ChooseLightToUpdatePage;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("PhoenixSH");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // === Main Content Area with CardLayout ===
        JPanel pageContainer = new JPanel(new CardLayout());
        getContentPane().add(pageContainer);

        // === Navigator Setup ===
        PageNavigator.initialize(pageContainer);

        // === Page Registration ===
        PageNavigator.registerPage(50, new WelcomeMenuPage());      // 🟩 Welcome
        PageNavigator.registerPage(100, new DeviceSettingsPage());  // 🛠️ Device Settings
        PageNavigator.registerPage(110, new AddDeviceMenuPage());   // ➕ Add Device
        PageNavigator.registerPage(112, new ChooseDevice4UpdatePage()); // 🔧 Choose Device/Sensor for Update
        PageNavigator.registerPage(120, new ChooseLightToUpdatePage(0)); // 💡 Light Update Matrix - Page 0
        PageNavigator.registerPage(200, new ChooseDeviceCtrlPage());    // 🎛️ Choose Device

        // === Initial Page ===
        PageNavigator.goToPage(50);
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            if (!XlWorkbookUtils.ensureFileExists()) {
                // GUI fallback already handled inside ensureFileExists()
                return;
            }

            System.out.println("✅ MainWindow: Launch triggered.");
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
