package devices;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DeviceStorage;
import storage.XlCreator;
import utils.Log;
import utils.NotificationService;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

public class DeviceFactory {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, Device> devices = new HashMap<>();

    // 🔧 Hook for injecting test behavior
    private static DeviceCreator overrideDeviceCreator;

    public static void setDeviceCreator(DeviceCreator creator) {
        overrideDeviceCreator = creator;
    }

    public static Device createDevice(
            DeviceType type,
            String id,
            String name,
            Clock clock,
            Map<String, Device> allDevices
    ) {
        // 🔄 Optional override for testing
        if (overrideDeviceCreator != null) {
            return overrideDeviceCreator.create(id, name, clock, allDevices);
        }

        switch (type) {
            case LIGHT -> {
                boolean savedState = getSavedState(id);
                double autoOnThreshold = 1024.0;
                double autoOffThreshold = 1050.0;
                boolean autoEnabled = false;

                try (FileInputStream fis = new FileInputStream(XlCreator.getFilePath().toFile());
                     Workbook workbook = new XSSFWorkbook(fis)) {

                    Sheet sheet = workbook.getSheet("Devices");
                    if (sheet != null) {
                        for (Row row : sheet) {
                            if (row.getRowNum() == 0) continue;
                            String deviceId = row.getCell(1).getStringCellValue();
                            if (deviceId.equalsIgnoreCase(id)) {
                                autoOnThreshold = Optional.ofNullable(row.getCell(6)).map(Cell::getNumericCellValue).orElse(autoOnThreshold);
                                autoOffThreshold = Optional.ofNullable(row.getCell(7)).map(Cell::getNumericCellValue).orElse(autoOffThreshold);

                                Cell autoCell = row.getCell(5);
                                if (autoCell != null) {
                                    autoEnabled = switch (autoCell.getCellType()) {
                                        case BOOLEAN -> autoCell.getBooleanCellValue();
                                        case STRING -> Boolean.parseBoolean(autoCell.getStringCellValue().trim());
                                        default -> false;
                                    };
                                }
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.error("🛑 Failed to load threshold values for " + id + ": " + e.getMessage());
                }

                Light light = new Light(id, name, clock, savedState, autoOnThreshold, autoOffThreshold);
                light.setAutomationEnabled(autoEnabled);
                return light;
            }

            case DRYER -> {
                System.out.print("Enter the brand of the Dryer: ");
                String brand = scanner.nextLine().trim();
                System.out.print("Enter the model of the Dryer: ");
                String model = scanner.nextLine().trim();
                return new Dryer(id, name, brand, model, clock);
            }

            case WASHING_MACHINE -> {
                System.out.print("Enter the brand of the Washing Machine: ");
                String brand = scanner.nextLine().trim();
                System.out.print("Enter the model of the Washing Machine: ");
                String model = scanner.nextLine().trim();
                return new WashingMachine(id, name, brand, model, clock);
            }

            case THERMOSTAT -> {
                Set<String> allIds = new HashSet<>(DeviceStorage.getDevices().keySet());
                String newId = XlCreator.getNextAvailableId("TH", allIds);
                boolean savedState = getSavedState(newId);
                NotificationService ns = new NotificationService();
                return new Thermostat(newId, name, 25.0, ns, clock);
            }

            default -> throw new IllegalArgumentException("Unsupported device type: " + type);
        }
    }

    public static Device createDeviceByType(String typeName, String id, String name, Clock clock, Map<String, Device> allDevices) {
        try {
            DeviceType type = DeviceType.fromString(typeName);
            System.out.println("🔍 DeviceFactory - Processing Type: '" + type + "'");
            return createDevice(type, id, name, clock, allDevices);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported device type: " + typeName);
        }
    }

    public static boolean getSavedState(String deviceId) {
        Device device = devices.get(deviceId);
        return device != null && device.isOn();
    }

    public static Map<String, Device> getDevices() {
        return devices;
    }

    // 🧪 Hookable creator for tests
    @FunctionalInterface
    public interface DeviceCreator {
        Device create(String id, String name, Clock clock, Map<String, Device> devices);
    }
}
