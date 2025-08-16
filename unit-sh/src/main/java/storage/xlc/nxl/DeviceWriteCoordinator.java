package storage.xlc.nxl;

import devices.Device;
import devices.SmartLight;
import org.apache.poi.ss.usermodel.*;
import storage.xlc.XlDeviceManager;
import storage.xlc.XlSmartLightManager;
import storage.xlc.XlWorkbookUtils;

public class DeviceWriteCoordinator {
    public static void writeDeviceToWorkbook(Device device) {
        try {
            Workbook workbook = XlWorkbookUtils.getWorkbook(XlWorkbookUtils.getFilePath().toString());

            // ✅ Always write to Devices sheet
            Sheet deviceSheet = workbook.getSheet("Devices");
            if (deviceSheet == null) {
                deviceSheet = workbook.createSheet("Devices");
                // Optional: createHeaderRow(deviceSheet);
            }

            int lastRow = deviceSheet.getLastRowNum();
            Row row = deviceSheet.createRow(lastRow + 1);
            XlDeviceManager.writeDeviceRow(device, row); // ← This gets called every time now

            // 🌈 SmartLight gets extra treatment
            if (device instanceof SmartLight sl) {
                XlSmartLightManager.updateSmartLight(workbook, sl);
            }

            // 💾 Save changes
            XlWorkbookUtils.saveWorkbook(workbook, XlWorkbookUtils.getFilePath().toString());
            System.out.println("📁 Saved device to workbook: " + device.getId());

        } catch (Exception e) {
            System.out.println("❌ Failed to write device: " + e.getMessage());
            e.printStackTrace();
        }
    }
}