package storage.xlc;

import devices.SmartLight;
import devices.actions.ApprovedDeviceModel;
import devices.actions.SmartLightColorMode;
import devices.actions.SmartLightEffect;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DeviceStorage;
import storage.xlc.sheetsCommand.SmartLightSheetCommand;
import utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XlSmartLightManager {

    private static final Clock clock = Clock.systemDefaultZone();
//    private static final String SHEET_DEVICES = "Devices";

    private static final String SHEET_SMART_LIGHTS = "Smart_light_Control";

    // 📥 Load SmartLights using DEVICE_ID only
    public static Map<String, SmartLight> loadSmartLights() {
        Map<String, SmartLight> loadedLights = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(XlWorkbookUtils.getFilePath().toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet controlSheet = workbook.getSheet(SHEET_SMART_LIGHTS);
            if (controlSheet == null) {
                Log.warn("⚠️ Sheet 'Smart_light_Control' not found.");
                return loadedLights;
            }

            Map<SmartLightSheetCommand, Integer> columnMap = SmartLightSheetCommand.getColumnMap();

            for (Row controlRow : controlSheet) {
                if (controlRow.getRowNum() == 0) continue;

                try {
                    String deviceId  = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.DEVICE_ID)).trim();
                    String name      = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.NAME)).trim();
                    String brand     = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.BRAND)).trim();
                    String model     = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.MODEL)).trim();
                    String redStr    = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.RED)).trim();
                    String greenStr  = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.GREEN)).trim();
                    String blueStr   = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.BLUE)).trim();

                    int red   = parseIntFromCell(redStr, 255);
                    int green = parseIntFromCell(greenStr, 222);
                    int blue  = parseIntFromCell(blueStr, 111);

                    // 🚀 Load automation settings straight from control sheet
                    boolean automationEnabled = false;
                    double autoOnThreshold = 1024.0;

                    String autoEnabledStr = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.AUTO_ENABLED)).trim();
                    String autoOnStr      = getCellValue(controlRow, columnMap.get(SmartLightSheetCommand.AUTO_ON)).trim();

                    automationEnabled = "1".equals(autoEnabledStr) || "true".equalsIgnoreCase(autoEnabledStr);
                    try {
                        autoOnThreshold = Double.parseDouble(autoOnStr);
                    } catch (Exception ignored) {}

                    ApprovedDeviceModel approvedModel = ApprovedDeviceModel.lookup(brand, model);
                    SmartLight light = new SmartLight(deviceId, name, approvedModel, clock, automationEnabled);
                    SmartLightColorMode mode = SmartLightColorMode.matchColorMode(red, green, blue);

                    light.setColorMode(mode);
                    light.setLiteFx(SmartLightEffect.NONE);
                    light.setAutoThreshold(autoOnThreshold, true);

                    System.out.printf("🌈 SmartLight [%s] initialized → Mode: %s | RGB(%d,%d,%d) | FX: %s | Power: OFF%n",
                            light.getName(),
                            mode.getLabel(),
                            mode.getRed(), mode.getGreen(), mode.getBlue(),
                            SmartLightEffect.NONE.name());

                    loadedLights.put(deviceId, light);
                    Log.debug("📥 SmartLight loaded: " + deviceId + " | AutoOp: " + automationEnabled + " | ON=" + autoOnThreshold);

                } catch (Exception e) {
                    Log.warn("❌ Invalid SmartLight row #" + controlRow.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Log.error("❌ Could not load SmartLights: " + e.getMessage());
        }

        return loadedLights;
    }

    public static boolean updateSmartLight(Workbook workbook, SmartLight light) {
        boolean updated = false;
        try {
            Sheet sheet = workbook.getSheet(SHEET_SMART_LIGHTS);
            if (sheet == null) {
                sheet = workbook.createSheet(SHEET_SMART_LIGHTS);
                createHeaderRow(sheet);
                Log.warn("⚠️ Sheet not found. Created new one: " + SHEET_SMART_LIGHTS);
            }

            Map<SmartLightSheetCommand, Integer> columnMap = SmartLightSheetCommand.getColumnMap();
            removeDuplicateSmartLightRows(sheet, light.getId(), columnMap);

            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            writeDeviceSmartLightControlRow(light, row);

            // 🧠 Debug: Log full SmartLight state before saving
            Log.debug("🧠 SmartLight update: ID=" + light.getId()
                    + ", AUTO_ENABLED=" + light.isAutomationEnabled()
                    + ", THRESHOLD=" + light.getAutoThreshold()
                    + ", COLOR_MODE=" + light.getColorMode().getLabel()
                    + ", RGB=[" + light.getColorMode().getRed() + "," + light.getColorMode().getGreen() + "," + light.getColorMode().getBlue() + "]"
                    + ", FX_MODE=" + light.getLiteFx().name()
                    + ", ACTIONS=" + light.getSupportedActionsAsText());

            XlWorkbookUtils.saveWorkbook(workbook, XlWorkbookUtils.getFilePath().toString());
            updated = true;
            Log.info("🌈 SmartLight control updated for device: " + light.getId());
        } catch (Exception e) {
            Log.error("❌ Failed to update SmartLight: " + e.getMessage());
        }
        return updated;
    }


    private static void removeDuplicateSmartLightRows(Sheet sheet, String deviceId, Map<SmartLightSheetCommand, Integer> columnMap) {
        List<Integer> rowsToRemove = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String type = getCellValue(row, columnMap.get(SmartLightSheetCommand.TYPE));
            String id = getCellValue(row, columnMap.get(SmartLightSheetCommand.DEVICE_ID));

            if (type == null || id == null || id.isBlank()) continue;

            // 🔍 Optional: Print row summary for debugging
            System.out.println("👀 Row " + row.getRowNum() + " → TYPE=" + type + ", ID=" + id);

            if (type.equals("SMART_LIGHT") && deviceId.equals(id)) {
                rowsToRemove.add(row.getRowNum());
            }
        }

        for (int rowNum : rowsToRemove) {
            Row row = sheet.getRow(rowNum);
            if (row != null) sheet.removeRow(row);
        }

        if (!rowsToRemove.isEmpty()) {
            Log.warn("🧨 Removed " + rowsToRemove.size() + " duplicate SmartLight rows for device: " + deviceId);
        }
    }

    private static int parseIntFromCell(String value, int fallback) {
        try {
            double raw = Double.parseDouble(value.trim());
            return (int) Math.round(raw);
        } catch (Exception e) {
            Log.warn("⚠️ Failed to parse RGB value: '" + value + "' — using fallback: " + fallback);
            return fallback;
        }
    }


    // ✍️ Write SmartLight with DEVICE_ID only
    public static boolean writeSmartLight(SmartLight light) {
        try {
            Workbook workbook = XlWorkbookUtils.getWorkbook(XlWorkbookUtils.getFilePath().toString());

            // === Smart_Light_Control Sheet ===
            Sheet controlSheet = workbook.getSheet(SHEET_SMART_LIGHTS);
            if (controlSheet == null) {
                controlSheet = workbook.createSheet(SHEET_SMART_LIGHTS);
                createHeaderRow(controlSheet);
            }
            Map<SmartLightSheetCommand, Integer> controlColumnMap = SmartLightSheetCommand.getColumnMap();
            removeDuplicateSmartLightRows(controlSheet, light.getId(), controlColumnMap);

            int controlRowIndex = controlSheet.getLastRowNum() + 1;
            Row controlRow = controlSheet.createRow(controlRowIndex);
            writeDeviceSmartLightControlRow(light, controlRow);

            // === Final Save ===
            try (FileOutputStream fos = new FileOutputStream(XlWorkbookUtils.getFilePath().toFile())) {
                workbook.write(fos);
                workbook.close();
            }

            Log.info("✅ SmartLight written to Devices and Control: " + light.getId());
            return true;

        } catch (IOException e) {
            Log.error("❌ Failed dual-write for SmartLight: " + e.getMessage());
            return false;
        }
    }

    public static void writeDeviceSmartLightControlRow(SmartLight smartLight, Row row) {
        if (smartLight != null && row != null && row.getSheet() != null) {
            SmartLightColorMode colorMode = smartLight.getColorMode();

            String colorModeLabel = (colorMode != null)
                    ? colorMode.getLabel()
                    : "None";

            String rgb = (colorMode != null)
                    ? String.format("%d,%d,%d", colorMode.getRed(), colorMode.getGreen(), colorMode.getBlue())
                    : "N/A";

            System.out.printf("🧾 Writing SmartLight [%s] (ID: %s, Threshold: %.2f, Color Mode: %s, RGB: [%s]) → Sheet: %s%n",
                    smartLight.getName(),
                    smartLight.getId(),
                    smartLight.getAutoThreshold(),
                    colorModeLabel,
                    rgb,
                    row.getSheet().getSheetName());
        }

        Map<SmartLightSheetCommand, Integer> smartMap = SmartLightSheetCommand.getColumnMap();

        // Basic Device Info
        row.createCell(smartMap.get(SmartLightSheetCommand.TYPE)).setCellValue(smartLight.getType().name());
        row.createCell(smartMap.get(SmartLightSheetCommand.DEVICE_ID)).setCellValue(smartLight.getId());
        row.createCell(smartMap.get(SmartLightSheetCommand.NAME)).setCellValue(smartLight.getName());
        row.createCell(smartMap.get(SmartLightSheetCommand.BRAND)).setCellValue(smartLight.getBrand());
        row.createCell(smartMap.get(SmartLightSheetCommand.MODEL)).setCellValue(smartLight.getModel());
        row.createCell(smartMap.get(SmartLightSheetCommand.AUTO_ENABLED))
                .setCellValue(smartLight.isAutomationEnabled() ? "1" : "0");
        row.createCell(smartMap.get(SmartLightSheetCommand.AUTO_ON)).setCellValue(smartLight.getAutoThreshold());

        SmartLightColorMode colorMode = smartLight.getColorMode();
        SmartLightEffect fxMode = smartLight.getLiteFx();

        if (colorMode != null) {
            row.createCell(smartMap.get(SmartLightSheetCommand.COLOUR_MODE)).setCellValue(colorMode.name());

            row.createCell(smartMap.get(SmartLightSheetCommand.RED)).setCellValue(colorMode.getRed());
            row.createCell(smartMap.get(SmartLightSheetCommand.GREEN)).setCellValue(colorMode.getGreen());
            row.createCell(smartMap.get(SmartLightSheetCommand.BLUE)).setCellValue(colorMode.getBlue());
        }

        if (fxMode != null) {
            row.createCell(smartMap.get(SmartLightSheetCommand.FX_MODE)).setCellValue(fxMode.name());
        }

        row.createCell(smartMap.get(SmartLightSheetCommand.LAST_UPDATED))
                    .setCellValue(smartLight.getUpdatedTimestamp());
        }

    // 🧾 Create header row (DEVICE_ID only)
    private static void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        int col = 0;
        for (SmartLightSheetCommand cmd : SmartLightSheetCommand.values()) {
            header.createCell(col++).setCellValue(cmd.label());
        }
    }

    // 🔧 Helper
    private static String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                return String.valueOf((int) Math.round(cell.getNumericCellValue()));
            case FORMULA: {
                FormulaEvaluator evaluator = row.getSheet().getWorkbook()
                        .getCreationHelper()
                        .createFormulaEvaluator();
                CellValue evaluated = evaluator.evaluate(cell);

                switch (evaluated.getCellType()) {
                    case BOOLEAN:
                        return Boolean.toString(evaluated.getBooleanValue());
                    case NUMERIC:
                        return String.valueOf((int) Math.round(evaluated.getNumberValue()));
                    case STRING:
                        return evaluated.getStringValue().trim();
                    default:
                        return "";
                }
            }
            default:
                return cell.toString().trim();
        }
    }
    public static void createAndWriteSmartLight(String deviceId) {
        File file = XlWorkbookUtils.getFilePath().toFile();

        if (!file.exists()) {
            Log.error("❌ Workbook file does not exist. Aborting SmartLight creation.");
            return;
        }

        if (!XlWorkbookUtils.isExcelFileHealthy(file)) {
            Log.error("❌ Workbook file is corrupt or unreadable. Aborting SmartLight creation.");
            return;
        }

        if (DeviceStorage.getDevices().containsKey(deviceId)) {
            Log.warn("⚠️ Device ID already exists in DeviceStorage → ID: " + deviceId);
            return;
        }

        ApprovedDeviceModel approvedModel = SmartLight.lookup("Calex", "A60E27");
        SmartLight smartLight = new SmartLight(deviceId, "Smart Light", approvedModel, clock, false);

        boolean added = DeviceStorage.addDevice(smartLight);
        if (!added) {
            Log.warn("⚠️ Failed to add SmartLight to DeviceStorage → ID: " + deviceId);
            return;
        }

        Workbook workbook = XlWorkbookUtils.loadWorkbook();
        if (workbook == null) {
            Log.error("❌ Could not load workbook for SmartLight record.");
            return;
        }

        boolean success = updateSmartLight(workbook, smartLight);
        if (success) {
            System.out.println("✅ SmartLight record safely created and written for device ID: " + deviceId);
        } else {
            Log.warn("❌ Failed to write SmartLight record for ID " + deviceId);
        }
    }



}