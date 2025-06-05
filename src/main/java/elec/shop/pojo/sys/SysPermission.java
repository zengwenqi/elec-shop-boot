package elec.shop.pojo.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;

/**
 * 权限表
 * @TableName sys_permission
 */
@TableName(value ="sys_permission")
@Data
public class SysPermission extends BaseEntity implements Serializable {
    /**
     * 权限ID
     */
    @TableId
    private Long permissionId;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限名称”
     */
    private String permissionName;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限类型：1-菜单 2-按钮 3-接口
     */
    private Integer permissionType;

    /**
     * 路径，若为菜单权限，存储菜单路由路径；若为接口权限，存储接口地址等
     */
    private String path;

    /**
     * 组件路径，若为菜单权限，存储对应前端组件的路径
     */
    private String component;

    /**
     * 重定向路径，用于菜单权限的路由重定向
     */
    private String redirect;

    /**
     * 图标，菜单权限对应的展示图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 是否隐藏：0-显示 1-隐藏
     */
    private Integer hidden;

    /**
     * 是否缓存：0-不缓存 1-缓存
     */
    private Integer keepAlive;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
