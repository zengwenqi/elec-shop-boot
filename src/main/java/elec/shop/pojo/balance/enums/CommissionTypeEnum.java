package elec.shop.pojo.balance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 佣金类型枚举
 */
@Getter
@AllArgsConstructor
public enum CommissionTypeEnum {
    
    /**
     * 固定金额
     */
    FIXED_AMOUNT(1, "固定金额"),
    
    /**
     * 百分比
     */
    PERCENTAGE(2, "百分比");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static CommissionTypeEnum getByCode(Integer code) {
        for (CommissionTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        CommissionTypeEnum typeEnum = getByCode(code);
        return typeEnum != null ? typeEnum.getDesc() : "未知类型";
    }
}