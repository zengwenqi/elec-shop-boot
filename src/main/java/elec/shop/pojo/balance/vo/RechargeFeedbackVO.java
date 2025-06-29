package elec.shop.pojo.balance.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("充值反馈VO")
public class RechargeFeedbackVO {

    @ExcelProperty("ID")
    @ColumnWidth(10)
    @ApiModelProperty("主键ID")
    private Long id;

    @ExcelProperty("反馈编号")
    @ColumnWidth(20)
    @ApiModelProperty("反馈编号")
    private String feedbackNo;

    @ExcelProperty("充值单号")
    @ColumnWidth(20)
    @ApiModelProperty("充值单号")
    private String rechargeNo;

    @ExcelProperty("充值金额")
    @ColumnWidth(15)
    @ApiModelProperty("充值金额")
    private BigDecimal money;

    @ExcelProperty("提交用户ID")
    @ColumnWidth(15)
    @ApiModelProperty("提交用户ID")
    private Long userId;

    @ExcelProperty("提交人姓名")
    @ColumnWidth(15)
    @ApiModelProperty("提交人姓名")
    private String submitter;

    @ExcelProperty("反馈类型")
    @ColumnWidth(20)
    @ApiModelProperty("反馈类型：AMOUNT_NOT_RECEIVED-金额未到账，AMOUNT_ERROR-金额错误，OTHER-其他问题")
    private String type;

    @ExcelProperty("图片凭证")
    @ColumnWidth(50)
    @ApiModelProperty("反馈图片凭证，存储图片路径列表")
    private List<String> images;

    @ExcelProperty("问题描述")
    @ColumnWidth(30)
    @ApiModelProperty("问题描述")
    private String description;

    @ExcelProperty("状态")
    @ColumnWidth(15)
    @ApiModelProperty("状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已驳回")
    private String status;

    @ExcelProperty("处理人ID")
    @ColumnWidth(15)
    @ApiModelProperty("处理人ID")
    private Long handlerId;

    @ExcelProperty("处理人姓名")
    @ColumnWidth(15)
    @ApiModelProperty("处理人姓名")
    private String handler;

    @ExcelProperty("处理时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("处理时间")
    private Date handleTime;
}
