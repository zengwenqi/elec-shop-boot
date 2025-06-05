package elec.shop.pojo.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity implements Serializable {
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private String createdBy; // 创建人

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private String updatedBy; // 更新人

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt; // 更新时间

    @TableLogic // 逻辑删除字段
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted; // 删除标记（0-未删除，1-已删除）

    @TableField(fill = FieldFill.UPDATE) // 逻辑删除时间（删除时填充）
    private Date deletedAt; // 删除时间

    @Version // 乐观锁版本号
    @TableField(fill = FieldFill.INSERT)
    private Integer version; // 版本号

    @TableField(fill = FieldFill.INSERT)
    private Integer tenantId; // 租户ID
}
