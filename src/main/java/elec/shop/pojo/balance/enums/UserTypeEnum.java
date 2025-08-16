package elec.shop.pojo.balance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    
    /**
     * 采购员
     */
    PURCHASER(1, "采购员"),
    
    /**
     * 管理员
     */
    ADMIN(2, "管理员");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static UserTypeEnum getByCode(Integer code) {
        for (UserTypeEnum typeEnum : values()) {
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
        UserTypeEnum typeEnum = getByCode(code);
        return typeEnum != null ? typeEnum.getDesc() : "未知类型";
    }
}