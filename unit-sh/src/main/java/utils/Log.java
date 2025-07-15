package utils;

import org.apache.poi.ss.usermodel.Workbook;

public class Log {
    public static final boolean DEBUG_MODE = false; // Set true when needed

    public static void debug(String message) {
        if (DEBUG_MODE) System.out.println(message);
    }

    public static void info(String message) {
        System.out.println("ℹ️ " + message);
    }

    public static void warn(String message) {
        System.out.println("⚠️ " + message);
    }

    public static void error(String message) {
        System.err.println("❌ " + message);
    }
    public static void debugSheetNames(Workbook workbook) {
        if (DEBUG_MODE) {
            System.out.println("💾 Sheets in memory:");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                System.out.println(" - " + workbook.getSheetName(i));
            }
        }
    }
    public static void debugf(String format, Object... args) {
        if (DEBUG_MODE) {
            System.out.printf(format, args);
        }
    }


}
