package devices;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    private static final Clock clock = Clock.systemDefaultZone();
    private static final Scanner scanner = new Scanner(System.in);


    // 🌟 The method you need — based on DeviceType enum
    public static Device createDevice(
            DeviceType type,
            String id,
            String name,
            Clock clock,
            Map<String, Device> allDevices
    ) {
        switch (type) {
            case LIGHT -> {
                boolean savedState = getSavedState(id);

                double autoOnThreshold = 1024.0;
                double autoOffThreshold = 1050.0;
                boolean autoEnabled = false; // 🧠 Default false

                try (FileInputStream fis = new FileInputStream(XlCreator.getFilePath().toFile());
                     Workbook workbook = new XSSFWorkbook(fis)) {

                    Sheet deviceSheet = workbook.getSheet("Devices");
                    if (deviceSheet != null) {
                        for (Row row : deviceSheet) {
                            if (row.getRowNum() == 0) continue;
                            String deviceId = row.getCell(1).getStringCellValue();
                            if (deviceId.equalsIgnoreCase(id)) {
                                autoOnThreshold = row.getCell(6) != null ? row.getCell(6).getNumericCellValue() : autoOnThreshold;
                                autoOffThreshold = row.getCell(7) != null ? row.getCell(7).getNumericCellValue() : autoOffThreshold;

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
                light.setAutomationEnabled(autoEnabled); // 💡 Crucial: apply loaded AutoOp flag

                return light;
            }




            case DRYER -> {
                System.out.print("Enter the brand of the Dryer: ");
                String brand = scanner.nextLine().trim();

                System.out.print("Enter the model of the Dryer: ");
                String model = scanner.nextLine().trim();

                return new Dryer(id, name, brand, model, clock); // ✅ Just use the passed ID!
            }
            case WASHING_MACHINE -> {
                System.out.print("Enter the brand of the Washing Machine: ");
                String brand = scanner.nextLine().trim();

                System.out.print("Enter the model of the Washing Machine: ");
                String model = scanner.nextLine().trim();

                return new WashingMachine(id, name, brand, model, clock); // ✅ Just use the passed ID!
            }


            case THERMOSTAT -> {
                Set<String> allIds = new HashSet<>(DeviceStorage.getDevices().keySet()); // ✅ Retrieve existing IDs
                String newId = XlCreator.getNextAvailableId("TH", allIds); // ✅ Generate unique ID
                boolean savedState = getSavedState(newId); // ✅ Retrieve last known state

                NotificationService ns = new NotificationService(); // ✅ Ensure notifications work
                return new Thermostat(newId, name, 25.0, ns, clock); // ✅ Pass required parameters
            }

            default -> throw new IllegalArgumentException("Unsupported device type: " + type);
        }

    }

    public static boolean getSavedState(String deviceId) {
        Device device = devices.get(deviceId);
        if (device != null) {
            return device.isOn(); // ✅ Pulls last known state
        }
        return false; // ✅ Defaults to OFF if no prior state exists
    }
    public static Device createDeviceByType(String typeName, String id, String name, Clock clock, Map<String, Device> allDevices) {
        try {
            DeviceType type = DeviceType.fromString(typeName); // ✅ now uses your tested logic
            System.out.println("🔍 DeviceFactory - Processing Type: '" + type + "' (Expected Types: " + Arrays.toString(DeviceType.values()) + ")");
            return createDevice(type, id, name, clock, allDevices);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or unsupported device type: " + typeName);
        }
    }

    private static final Map<String, Device> devices = new HashMap<>();

    public static Map<String, Device> getDevices() {
        return devices;
    }

}
