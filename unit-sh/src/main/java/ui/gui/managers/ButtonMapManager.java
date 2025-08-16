package ui.gui.managers;

import devices.Device;
import devices.DeviceType;
import devices.actions.LiveDeviceState;
import storage.DeviceStorage;
import ui.gui.PageNavigator;
import utils.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ButtonMapManager {
    private static final int PAGE_SIZE = 10;
    private static final int BASE_LIGHT_PAGE_ID = 120;
    private static final int BASE_LIGHT_SMART_PAGE_ID = 140;

    // 🧭 Improved navigation
    private static void goToPage(List<DeviceType> types, int pageIndex, int basePageId) {
        int pageId = basePageId + pageIndex;

        // ✅ Rebuild the grid panel
        JPanel gridPanel = renderGridFromDevices(getDevicesByTypes(types.toArray(new DeviceType[0])), pageIndex, basePageId, types);

        // ✅ Register the page
        PageNavigator.registerPage(pageId, gridPanel);

        // ✅ Navigate to the page
        PageNavigator.goToPage(pageId);

        // ✅ Repaint and revalidate
        Component pageComponent = PageNavigator.getPage(pageId); // Make sure this method exists
        if (pageComponent instanceof JPanel panel) {
            panel.revalidate();
            panel.repaint();
            System.out.println("🧪 Revalidated and repainted page " + pageId);
        } else {
            System.out.println("⚠️ Page " + pageId + " is not a JPanel, cannot repaint.");
        }
    }


    // 🧠 Filter by type
    private static List<Device> getDevicesByTypes(DeviceType... types) {
//        DeviceStorage.reloadFromExcel(); // 🔁 Always reload
        Set<DeviceType> typeSet = new HashSet<>(Arrays.asList(types));

        return DeviceStorage.getDevices().values().stream()
                .filter(d -> typeSet.contains(d.getType()))
                .sorted(Comparator.comparing(Device::getId))
                .collect(Collectors.toList());
    }

    // 🔧 Page renderer
    public static JPanel renderPageForTypes(DeviceType[] types, int pageIndex, int basePageId) {
        // Get all devices matching the given types
        List<Device> allFiltered = getDevicesByTypes(types);

        // Clamp page index to valid range
        int maxPage = Math.max(0, (int) Math.ceil((double) allFiltered.size() / PAGE_SIZE) - 1);
        int clampedPageIndex = Math.min(pageIndex, maxPage);

        // Fetch devices for the clamped page
        List<Device> devices = allFiltered.stream()
                .skip((long) clampedPageIndex * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .toList();

        // Debug logs
        System.out.println("🔍 Button map keys: " + GuiStateManager.getDeviceButtonMap().keySet());
        System.out.println("📦 Filtered devices for types: " + allFiltered.stream().map(Device::getId).toList());
        System.out.println("📦 Requested page index: " + pageIndex + " | Clamped to: " + clampedPageIndex);
        System.out.println("📦 Devices on page: " + devices.stream().map(Device::getId).toList());

        // Render the grid panel
        return renderGridFromDevices(devices, clampedPageIndex, basePageId, Arrays.asList(types));
    }

    private static JPanel renderGridFromDevices(List<Device> devices, int pageIndex, int basePageId, List<DeviceType> types) {
        int pageId = basePageId + pageIndex;

        JPanel gridPanel = new JPanel(new GridLayout(4, 3, 20, 20));
        gridPanel.setBackground(Theme.BACKGROUND_DARK);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        gridPanel.setPreferredSize(new Dimension(800, 360));

        System.out.println("🧱 Grid built with devices: " + devices.stream().map(Device::getId).toList());

        for (int i = 0; i < 9; i++) {
            if (i < devices.size()) {
                Device device = devices.get(i);
                JButton button = GuiStateManager.getButtonForDevice(device.getId());
                JButton clone = button != null ? cloneButton(button, device) : createNameButton(device, i);
                gridPanel.add(clone);
            } else {
                gridPanel.add(createPlaceholder());
            }
        }

        // ← Navigation button
        gridPanel.add(createNavButton("←", pageIndex > 0, () -> goToPage(types, pageIndex - 1, basePageId)));

        // 10th device button
        if (devices.size() >= 10) {
            Device device = devices.get(9);
            JButton button = GuiStateManager.getButtonForDevice(device.getId());
            gridPanel.add(button != null ? button : createNameButton(device, 9));
        } else {
            gridPanel.add(createPlaceholder());
        }

        // → Navigation button
        gridPanel.add(createNavButton("→", hasNextPage(types, pageIndex), () -> goToPage(types, pageIndex + 1, basePageId)));

        return gridPanel;
    }

    private static JButton cloneButton(JButton original, Device device) {
        JButton clone = new JButton(original.getText());
        clone.setFont(original.getFont());
        clone.setPreferredSize(original.getPreferredSize());
        clone.setFocusPainted(false);
        clone.setOpaque(true);
        clone.setContentAreaFilled(true);

        // Reapply theme based on device state
        boolean isOn = LiveDeviceState.isOn(device);
        if (device.getType() == DeviceType.SMART_LIGHT) {
            clone.setBackground(isOn ? Theme.SMART_ON_GREEN : Theme.SMART_OFF_GREEN);
        } else {
            clone.setBackground(isOn ? Theme.BASIC_ON_GREEN : Theme.BASIC_OFF_GREEN);
        }

        for (ActionListener al : original.getActionListeners()) {
            clone.addActionListener(al);
        }

        return clone;
    }

    private static boolean hasNextPage(List<DeviceType> types, int pageIndex) {
        long totalDevices = getDevicesByTypes(types.toArray(new DeviceType[0])).size();
        int totalPages = (int) Math.ceil((double) totalDevices / PAGE_SIZE);
        return pageIndex + 1 < totalPages;
    }

    // 🧱 Button builders
    public static JButton createNameButton(Device device, int position) {
        JButton button = new JButton();
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 60));

        if (device != null) {
            button.setText("<html><center>" + device.getName() + "</center></html>");

            boolean isOn = LiveDeviceState.isOn(device);

            if (device.getType() == DeviceType.SMART_LIGHT) {
                button.setBackground(isOn ? Theme.SMART_ON_GREEN : Theme.SMART_OFF_GREEN);
            } else {
                button.setBackground(isOn ? Theme.BASIC_ON_GREEN : Theme.BASIC_OFF_GREEN);
            }

            button.addActionListener(e -> {
                System.out.println("🔧 Selected for update: " + device.getName() + " [" + device.getId() + "]");
                GuiStateManager.refreshDeviceControlPage(device); // ✅ Use manager
            });
        } else {
            button.setText("N/A");
            button.setEnabled(false);
            button.setBackground(Theme.DARK_ORANGE);
        }

        return button;
    }

    private static JButton createNavButton(String label, boolean enabled, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Monospaced", Font.PLAIN, 14));
        btn.setEnabled(enabled);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private static JButton createPlaceholder() {
        JButton placeholder = new JButton("N/A");
        placeholder.setEnabled(false);
        placeholder.setBackground(Theme.DARK_ORANGE);
        placeholder.setBorder(BorderFactory.createLineBorder(Theme.BUTTON_GRAY));
        placeholder.setPreferredSize(new Dimension(160, 40));
        return placeholder;
    }
}