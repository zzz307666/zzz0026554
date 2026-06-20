package com.hhjt.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 工具类
 */
public class ExcelUtil {

    /**
     * 读取Excel文件，返回字符串二维数组
     * @param file Excel文件
     * @param sheetIndex 工作表索引（从0开始）
     * @param headerRows 跳过的表头行数
     * @return 数据列表
     */
    public static List<String[]> readExcel(MultipartFile file, int sheetIndex, int headerRows) throws IOException {
        List<String[]> dataList = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            if (sheet == null) {
                throw new IOException("工作表不存在");
            }
            
            // 从指定行开始读取
            for (int i = headerRows; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // 跳过空行
                if (isRowEmpty(row)) {
                    continue;
                }
                
                String[] rowData = new String[row.getLastCellNum()];
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    rowData[j] = getCellValue(cell);
                }
                
                dataList.add(rowData);
            }
        }
        
        return dataList;
    }

    /**
     * 判断行是否为空
     */
    private static boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取单元格值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 验证必填字段
     */
    public static boolean validateRequiredFields(String[] row, int[] requiredIndices) {
        for (int index : requiredIndices) {
            if (index >= row.length || row[index] == null || row[index].trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
