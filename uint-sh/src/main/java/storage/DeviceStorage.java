package storage;

import devices.Device;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.NotificationService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class DeviceStorage {

    private static final String EXCEL_FILE_NAME = "/home/nira/Documents/Shay/Fleur/unit-sh/uint-sh/DeviceList.xlsx";


    public static Map<String, Device> loadDevices(NotificationService notificationService) {
        return ExcelDeviceLoader.loadDevices(EXCEL_FILE_NAME);
    }

    public static void saveDevices(Map<String, Device> devices) {
        System.out.println("🛠️ saveDevices() was called!");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Devices");

            // 🔹 Write header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("TYPE");
            header.createCell(1).setCellValue("ID");
            header.createCell(2).setCellValue("NAME");
            header.createCell(3).setCellValue("BRAND");
            header.createCell(4).setCellValue("MODEL");
            header.createCell(5).setCellValue("ACTIONS");

            int rowIndex = 1;
            for (Device device : devices.values()) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(device.getClass().getSimpleName()); // or device.getType()
                row.createCell(1).setCellValue(device.getId());
                row.createCell(2).setCellValue(device.getName());
                row.createCell(3).setCellValue(device.getBrand());
                row.createCell(4).setCellValue(device.getModel());
                row.createCell(5).setCellValue(String.join(", ", device.getActions())); // comma-separated actions
            }

            System.out.println("📂 Saving to file: " + EXCEL_FILE_NAME);

            try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_NAME)) {
                workbook.write(fos);
            }

            System.out.println("✅ Devices saved to Excel successfully.");

        } catch (IOException e) {
            System.out.println("❌ Failed to save devices (Excel): " + e.getMessage());
        }
    }

}
