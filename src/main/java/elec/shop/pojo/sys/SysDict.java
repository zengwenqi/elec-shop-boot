package elec.shop.pojo.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;

/**
 * 数据字典表
 * @TableName sys_dict
 */
@TableName(value ="sys_dict")
@Data
public class SysDict extends BaseEntity implements Serializable {
    /**
     * 字典ID
     */
    @TableId
    private Long dictId;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
