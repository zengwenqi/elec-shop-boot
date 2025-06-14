package elec.shop.pojo.sys.enums;

public enum UserType {
    SUPER_ADMIN(1, "超级管理员"),
    ADMIN(2, "管理员"),
    PURCHASER(3, "采购员"),
    MERCHANT(4, "商户");

    private final Integer id;
    private final String name;

    // 构造函数
    UserType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // 根据ID获取对应的枚举实例
    public static UserType getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (UserType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    // 获取类型名称
    public String getName() {
        return name;
    }

    // 获取类型ID
    public Integer getId() {
        return id;
    }
}
