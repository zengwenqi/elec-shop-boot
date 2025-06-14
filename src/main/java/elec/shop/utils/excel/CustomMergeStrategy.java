package elec.shop.utils.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

public class CustomMergeStrategy extends AbstractMergeStrategy {
    
    private final Map<String, List<Integer>> orderRowMap = new HashMap<>(); // 订单号 -> 行号列表
    private static final int MERGE_COLUMN_COUNT = 23; // 0-22列需要合并
    private boolean dataCollected = false;

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (cell == null || cell.getRowIndex() == 0) {
            return;
        }

        int rowIndex = cell.getRowIndex();
        int colIndex = cell.getColumnIndex();

        // 第一阶段：收集所有数据
        if (!dataCollected && colIndex == 0) {
            String orderNo = getCellValue(cell);
            orderRowMap.computeIfAbsent(orderNo, k -> new ArrayList<>()).add(rowIndex);
            
            // 如果是最后一行，开始处理合并
            if (rowIndex == sheet.getLastRowNum()) {
                dataCollected = true;
                processMerge(sheet);
            }
        }
    }

    private void processMerge(Sheet sheet) {
        // 处理每个订单号的合并
        for (Map.Entry<String, List<Integer>> entry : orderRowMap.entrySet()) {
            List<Integer> rows = entry.getValue();
            if (rows.size() <= 1) continue;

            // 对每一列进行合并检查
            for (int col = 0; col < MERGE_COLUMN_COUNT; col++) {
                try {
                    // 获取第一行的值作为参考
                    String baseValue = getCellValue(sheet.getRow(rows.get(0)).getCell(col));
                    boolean canMerge = true;
                    
                    // 检查所有行的值是否相同
                    for (int i = 1; i < rows.size(); i++) {
                        String currentValue = getCellValue(sheet.getRow(rows.get(i)).getCell(col));
                        if (!baseValue.equals(currentValue)) {
                            canMerge = false;
                            break;
                        }
                    }

                    // 如果所有值相同且不为空，执行合并
                    if (canMerge && !baseValue.isEmpty()) {
                        int startRow = rows.get(0);
                        int endRow = rows.get(rows.size() - 1);
                        CellRangeAddress range = new CellRangeAddress(startRow, endRow, col, col);
                        sheet.addMergedRegion(range);
                    }
                } catch (Exception ignored) {
                    // 如果合并失败，继续下一列
                }
            }
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        try {
            CellType cellType = cell.getCellType();
            switch (cellType) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    }
                    return String.valueOf(numericValue);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e) {
                        return cell.getStringCellValue();
                    }
                case BLANK:
                    return "";
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
} 