package elec.shop.pojo.announcement;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 常见问题表
 * @TableName faq
 */
@TableName(value ="faq")
@Data
@EqualsAndHashCode(callSuper = true)
public class Faq extends BaseEntity implements Serializable {
    /**
     * FAQ ID
     */
    @TableId
    private Long faqId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 有帮助次数
     */
    private Integer helpfulCount;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
