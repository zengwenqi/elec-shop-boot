package elec.shop.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 范围查找工具类，通过二分查找高效定位目标值所在的范围
 * 适用于任何实现了Rangeable接口的实体类
 */
public class RangeSearchUtil<T extends RangeSearchUtil.Rangeable> {

    // 排序后的数据集（构造时初始化，复用查询）
    private final List<T> sortedDataList;

    /**
     * 构造器：初始化并排序数据
     * @param dataList 原始数据列表（需保证范围不重叠，否则可能返回首个匹配项）
     */
    public RangeSearchUtil(List<T> dataList) {
        // 防御性拷贝避免外部修改影响内部排序
        this.sortedDataList = new ArrayList<>(dataList);
        // 按最小值升序排序（二分查找的前提）
        Collections.sort(this.sortedDataList, Comparator.comparingDouble(Rangeable::getMin));
    }

    /**
     * 二分查找目标值所在的范围
     * @param target 目标值
     * @return 匹配的范围对象，无匹配时返回null
     */
    public T findMatchingRange(Double target) {
        int left = 0;
        int right = sortedDataList.size() - 1;

        while (left <= right) {
            // 计算中间索引（避免整数溢出）
            int mid = left + (right - left) / 2;
            T midData = sortedDataList.get(mid);

            if (target < midData.getMin()) {
                // 目标值小于当前范围最小值，搜索左半部分
                right = mid - 1;
            } else if (target > midData.getMax()) {
                // 目标值大于当前范围最大值，搜索右半部分
                left = mid + 1;
            } else {
                // 命中范围（min ≤ target ≤ max）
                return midData;
            }
        }
        // 无匹配范围
        return null;
    }

    /**
     * 范围接口：所有需要使用该工具类的实体类需实现此接口
     */
    public interface Rangeable {
        double getMin(); // 获取最小值阈值
        double getMax(); // 获取最大值阈值
    }
}
