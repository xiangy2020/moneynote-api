package cn.biq.mn.utils;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class ExcelUtils {
    /**
     * 安全地获取单元格的整数值。
     * 如果列不存在或单元格为空，则返回默认值。
     *
     * @param row            当前行
     * @param columnIndices  列名到列索引的映射
     * @param columnName     列名
     * @param defaultValue   默认值
     * @return               单元格的整数值或默认值
     */
    public static int getSafeIntCellValue(Row row, Map<String, Integer> columnIndices, String columnName, int defaultValue) {
        Integer columnIndex = columnIndices.get(columnName);
        if (columnIndex == null) { // 列不存在
            return defaultValue;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) { // 单元格为空或不是数值类型
            return defaultValue;
        }

        return (int) cell.getNumericCellValue();
    }

    /**
     * 安全地获取单元格的布尔值。
     * 如果列不存在或单元格为空，则返回默认值。
     *
     * @param row            当前行
     * @param columnIndices  列名到列索引的映射
     * @param columnName     列名
     * @param defaultValue   默认值
     * @return               单元格的布尔值或默认值
     */
    public static boolean getSafeBooleanCellValue(Row row, Map<String, Integer> columnIndices, String columnName, boolean defaultValue) {
        Integer columnIndex = columnIndices.get(columnName);
        if (columnIndex == null) { // 列不存在
            return defaultValue;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() != CellType.BOOLEAN) { // 单元格为空或不是布尔类型
            return defaultValue;
        }

        return cell.getBooleanCellValue();
    }

    /**
     * 安全地获取单元格的 double 值。
     * 如果列不存在或单元格为空，则返回默认值。
     *
     * @param row            当前行
     * @param columnIndices  列名到列索引的映射
     * @param columnName     列名
     * @param defaultValue   默认值
     * @return               单元格的 double 值或默认值
     */
    public static double getSafeDoubleCellValue(Row row, Map<String, Integer> columnIndices, String columnName, double defaultValue) {
        Integer columnIndex = columnIndices.get(columnName);
        if (columnIndex == null) { // 列不存在
            return defaultValue;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) { // 单元格为空或不是数值类型
            return defaultValue;
        }

        return cell.getNumericCellValue();
    }


    /**
     * 安全地获取单元格的字符串值。
     * 如果列不存在或单元格为空，则返回默认值。
     *
     * @param row            当前行
     * @param columnIndices  列名到列索引的映射
     * @param columnName     列名
     * @param defaultValue   默认值
     * @return               单元格的字符串值或默认值
     */
    public static String getSafeStringCellValue(Row row, Map<String, Integer> columnIndices, String columnName, String defaultValue) {
        Integer columnIndex = columnIndices.get(columnName);
        if (columnIndex == null) { // 列不存在
            return defaultValue;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) { // 单元格为空或空白
            return defaultValue;
        }

        // 处理不同类型的单元格
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return defaultValue;
        }
    }
}
