package ui.gui.pages;

import devices.Device;
import devices.DeviceFactory;
import devices.DeviceType;
import devices.actions.ApprovedDeviceModel;
import devices.actions.LiveDeviceState;
import sensors.SensorType;
import storage.DeviceStorage;
import storage.XlCreator;
import ui.gui.PageNavigator;
import ui.gui.managers.GuiStateManager;
import utils.ClockUtil;
import utils.DeviceIdManager;
import utils.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class AddDeviceMenuPage extends JPanel {
    public static final int PAGE_NUMBER = 110;

    public AddDeviceMenuPage() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(800, 120));
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createTitledBorder(null, "Add Device / Sensor",
                0, 0, new Font("Monospaced", Font.PLAIN, 14), Color.LIGHT_GRAY));

        JLabel title = new JLabel("📟 Register New Device into Smart System", JLabel.CENTER);
        title.setForeground(Color.LIGHT_GRAY);
        title.setFont(new Font("Monospaced", Font.BOLD, 16));
        header.add(title, BorderLayout.CENTER);

        return header;
    }

    private JPanel createFormPanel() {
        final ApprovedDeviceModel[] selectedModel = new ApprovedDeviceModel[1]; // mutable holder

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BACKGROUND_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        Dimension fieldSize = new Dimension(300, 30);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<DeviceType> deviceTypeCombo = new JComboBox<>(DeviceType.values());
        JComboBox<SensorType> sensorTypeCombo = new JComboBox<>(SensorType.values());
        sensorTypeCombo.setVisible(false);

        JComboBox<String> brandCombo = new JComboBox<>();
        JComboBox<String> modelCombo = new JComboBox<>();

        JTextField nameField = new JTextField();

        deviceTypeCombo.setMaximumSize(fieldSize);
        sensorTypeCombo.setMaximumSize(fieldSize);
        brandCombo.setMaximumSize(fieldSize);
        modelCombo.setMaximumSize(fieldSize);
        nameField.setMaximumSize(fieldSize);

        deviceTypeCombo.addActionListener(e -> {
            DeviceType selectedType = (DeviceType) deviceTypeCombo.getSelectedItem();
            sensorTypeCombo.setVisible(selectedType == DeviceType.SENSOR);

            List<ApprovedDeviceModel> models = ApprovedDeviceModel.getByType(selectedType);
            brandCombo.removeAllItems();
            modelCombo.removeAllItems();
            selectedModel[0] = null;

            models.stream()
                    .map(ApprovedDeviceModel::getBrand)
                    .distinct()
                    .forEach(brandCombo::addItem);

            brandCombo.setSelectedIndex(-1);
            modelCombo.setSelectedIndex(-1);
            revalidate(); repaint();
        });

        brandCombo.addActionListener(e -> {
            DeviceType selectedType = (DeviceType) deviceTypeCombo.getSelectedItem();
            String selectedBrand = (String) brandCombo.getSelectedItem();

            modelCombo.removeAllItems();
            selectedModel[0] = null;

            ApprovedDeviceModel.getByType(selectedType).stream()
                    .filter(m -> m.getBrand().equalsIgnoreCase(selectedBrand))
                    .forEach(m -> modelCombo.addItem(m.getModel()));
        });

        modelCombo.addActionListener(e -> {
            DeviceType selectedType = (DeviceType) deviceTypeCombo.getSelectedItem();
            String selectedBrand = (String) brandCombo.getSelectedItem();
            String selectedModelName = (String) modelCombo.getSelectedItem();

            selectedModel[0] = ApprovedDeviceModel.getByType(selectedType).stream()
                    .filter(m -> m.getBrand().equalsIgnoreCase(selectedBrand))
                    .filter(m -> m.getModel().equalsIgnoreCase(selectedModelName))
                    .findFirst()
                    .orElse(null);
        });

        JButton submitBtn = new JButton("Submit");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            DeviceType type = (DeviceType) deviceTypeCombo.getSelectedItem();
            Optional<SensorType> subtype = sensorTypeCombo.isVisible()
                    ? Optional.of((SensorType) sensorTypeCombo.getSelectedItem())
                    : Optional.empty();

            String name = nameField.getText().trim();

            if (name.isBlank()) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("⚠️ Please enter a valid name.");
                return;
            }

            boolean nameExists = DeviceStorage.getDevices().values().stream()
                    .anyMatch(d -> d.getName().equalsIgnoreCase(name));
            if (nameExists) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("❌ Name already exists: " + name);
                return;
            }

            ApprovedDeviceModel approvedModel = selectedModel[0];
            if (approvedModel == null) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("⚠️ Please select a valid brand and model.");
                return;
            }

            String brand = approvedModel.getBrand();
            String model = approvedModel.getModel();

            System.out.println("Selected brand: " + brand);
            System.out.println("Selected model: " + model);

            String prefix = switch (type) {
                case LIGHT -> "LI";
                case SMART_LIGHT -> "SL";
                case THERMOSTAT -> "TH";
                case DRYER -> "DR";
                case WASHING_MACHINE -> "WM";
                case SENSOR -> "SN";
                default -> "UN";
            };

            Clock clock = ClockUtil.getClock();
            String uniqueId = DeviceIdManager.getNextAvailableId(prefix, DeviceStorage.getDevices().keySet());

            Device newDevice = DeviceFactory.createDevice(
                    type,
                    uniqueId,
                    name,
                    clock,
                    DeviceStorage.getDevices(),
                    approvedModel,
                    brand,
                    model
            );

            if (newDevice == null) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("❌ Failed to create device.");
                return;
            }

            newDevice.setAddedTimestamp(ZonedDateTime.now(clock));
            DeviceStorage.add(newDevice);
            DeviceStorage.updateMemoryAfterExcelWrite(newDevice);
            boolean success = XlCreator.delegateDeviceUpdate(newDevice);
            if (!success) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("❌ Failed to write device to Excel.");
                return;
            }
            GuiStateManager.registerNewDevice(newDevice);
            LiveDeviceState.turnOn(newDevice); // or turnOff if preferred
            DeviceStorage.getDevices().put(newDevice.getId(), newDevice); // redundant if already added, but safe
            GuiStateManager.refreshDeviceMatrix(); // 🔁 Rebuild the button map
            System.out.println("✅ " + newDevice.getName() + " (" + newDevice.getId() + ") added to GUI button map successfully!");



            statusLabel.setForeground(Color.GREEN);
            statusLabel.setText("✅ Device registered: " + name);
        });

        form.add(new JLabel("Choose Device Type:")).setForeground(Color.LIGHT_GRAY);
        form.add(deviceTypeCombo);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Sensor Type (if applicable):")).setForeground(Color.LIGHT_GRAY);
        form.add(sensorTypeCombo);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Device Name:")).setForeground(Color.LIGHT_GRAY);
        form.add(nameField);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Brand:")).setForeground(Color.LIGHT_GRAY);
        form.add(brandCombo);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Model:")).setForeground(Color.LIGHT_GRAY);
        form.add(modelCombo);
        form.add(Box.createVerticalStrut(20));
        form.add(submitBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(statusLabel);

        return form;
    }


    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel pageLabel = new JLabel(String.format("Page %03d", PAGE_NUMBER));
        pageLabel.setForeground(Color.GREEN);
        pageLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JButton homeBtn = new JButton("Home");
        homeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        homeBtn.addActionListener(e -> PageNavigator.goToPage(50));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setToolTipText("Go back to previous page");
        backBtn.addActionListener(e -> PageNavigator.goToPage(100));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setBackground(Color.BLACK);
        navPanel.add(backBtn);
        navPanel.add(homeBtn);

        footer.add(pageLabel, BorderLayout.WEST);
        footer.add(navPanel, BorderLayout.EAST);

        return footer;
    }
}
