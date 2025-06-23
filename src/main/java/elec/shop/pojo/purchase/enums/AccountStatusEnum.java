package elec.shop.pojo.purchase.enums;

import elec.shop.pojo.announcement.enums.MessageTypeEnum;
import lombok.Getter;

@Getter
public enum AccountStatusEnum {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    CANCELLED(3, "注销");

    private Integer code;
    private String desc;

    AccountStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AccountStatusEnum getByCode(Integer code) {
        for (AccountStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
