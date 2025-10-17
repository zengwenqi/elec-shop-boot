package elec.shop.pojo.purchase.utils;

import java.util.HashMap;
import java.util.Map;

public class TaskStatus {
    // 任务状态映射：key为状态码，value为状态描述
    public static final Map<Integer, String> STATUS_MAP = new HashMap<Integer, String>() {{
        put(0, "待处理");
        put(1, "处理中");
        put(2, "待确认");
        put(3, "待采购");
        put(4, "已下单");
        put(5, "已出面单");
        put(6, "已完成");
        put(7, "已取消");
        put(8, "等待补价");
    }};

    // 示例：根据状态码获取状态描述
    public static String getStatusDesc(Integer code) {
        // 若状态码不存在，返回"未知状态"
        return STATUS_MAP.getOrDefault(code, "未知状态");
    }
}
