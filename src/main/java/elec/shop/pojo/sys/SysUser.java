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
 * 用户表
 * @TableName sys_user
 */
@TableName(value ="sys_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity implements Serializable {
    /**
     * 用户ID
     */
    @TableId
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 支付密码
     */
    private String paymentPassword;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 状态：0-禁用 1-启用 2-锁定
     */
    private Integer status;

    /**
     * 用户类型：1-超级管理员 2-管理员 3-采购员 4-商户
     */
    private Integer userType;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
