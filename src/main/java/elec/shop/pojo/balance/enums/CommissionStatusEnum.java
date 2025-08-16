package elec.shop.pojo.balance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 佣金状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommissionStatusEnum {
    
    /**
     * 待发放
     */
    PENDING(0, "待发放"),
    
    /**
     * 已发放
     */
    PAID(1, "已发放"),
    
    /**
     * 已取消
     */
    CANCELLED(2, "已取消");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static CommissionStatusEnum getByCode(Integer code) {
        for (CommissionStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        CommissionStatusEnum statusEnum = getByCode(code);
        return statusEnum != null ? statusEnum.getDesc() : "未知状态";
    }
}