package utils;

import org.apache.poi.ss.usermodel.*;

public class XlUtils {

    public static String getCellValue(Row row, int columnIndex) {
        if (row == null || columnIndex < 0) return "";

        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString(); // or format as needed
                    }
                    return String.valueOf(cell.getNumericCellValue());

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    return evaluateFormula(cell);

                case BLANK:
                case _NONE:
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private static String evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(cell);

            switch (cellValue.getCellType()) {
                case STRING:
                    return cellValue.getStringValue().trim();
                case NUMERIC:
                    return String.valueOf(cellValue.getNumberValue());
                case BOOLEAN:
                    return String.valueOf(cellValue.getBooleanValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}