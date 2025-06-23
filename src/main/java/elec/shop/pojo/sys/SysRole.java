package elec.shop.pojo.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表
 * @TableName sys_role
 */
@TableName(value ="sys_role")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity implements Serializable {
    /**
     * 角色ID
     */
    @TableId
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 描述，对角色功能、职责等的说明
     */
    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
