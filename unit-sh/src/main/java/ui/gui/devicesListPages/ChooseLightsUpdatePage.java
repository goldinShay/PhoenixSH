package ui.gui.devicesListPages;

import devices.DeviceType;
import ui.gui.PageNavigator;
import ui.gui.managers.ButtonMapManager;
import utils.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ChooseLightsUpdatePage extends JPanel {
    private final int pageIndex;
    private final List<DeviceType> deviceTypes;
    private final int basePageId;

    public ChooseLightsUpdatePage(int pageIndex, int basePageId, DeviceType... types) {
        this.pageIndex = pageIndex;
        this.deviceTypes = Arrays.asList(types);
        this.basePageId = basePageId;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMatrixPanel(types), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(800, 120));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "Choose Device",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel title = new JLabel("📟 Select Device to Update", JLabel.CENTER);
        title.setForeground(Color.LIGHT_GRAY);
        title.setFont(new Font("Monospaced", Font.BOLD, 16));
        title.setBounds(0, 40, 800, 40);

        header.add(title);
        return header;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(800, 60));
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page %03d", basePageId + pageIndex));
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtons.setBackground(Color.BLACK);

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back to previous menu");
        backBtn.addActionListener(e -> PageNavigator.goToPage(112));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        navButtons.add(backBtn);
        navButtons.add(homeBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navButtons, BorderLayout.EAST);
        return footer;
    }

    private JScrollPane createMatrixPanel(DeviceType[] types) {
        JPanel grid = ButtonMapManager.renderPageForTypes(types, pageIndex, basePageId + pageIndex);
        grid.setPreferredSize(new Dimension(800, 360)); // Constrain matrix size

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setPreferredSize(new Dimension(800, 360));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    public static ChooseLightsUpdatePage loadFresh(int pageIndex, int basePageId, DeviceType... types) {
        return new ChooseLightsUpdatePage(pageIndex, basePageId, types);
    }
}