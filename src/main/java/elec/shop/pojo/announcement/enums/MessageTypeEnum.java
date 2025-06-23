package elec.shop.pojo.announcement.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    SMS(1, "短信"),
    EMAIL(2, "邮件"),
    INTERNAL(3, "站内信"),
    SYSTEM_NOTICE(4, "系统公告"),
    PRIVATE_MESSAGE(5, "私信");

    private final Integer code;
    private final String desc;

    MessageTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageTypeEnum getByCode(Integer code) {
        for (MessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
