package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("采购任务状态变更请求")
public class TaskStatusChangeDTO {
    @ApiModelProperty(value = "任务ID/订单ID", required = true)
    private Long taskId;

    @ApiModelProperty(value = "新的任务状态")
    private Integer newStatus;

    @ApiModelProperty(value = "实际采购价格")
    private BigDecimal realTotalAmount;

    @ApiModelProperty(value = "服务费")
    private BigDecimal serviceCharge;

    @ApiModelProperty(value = "平台订单号")
    private String remarkOrderNo;

    @ApiModelProperty("状态变更备注（可选）")
    private String remark;

    @ApiModelProperty("采购订单回执图片")
    private List<ReceiptFiles> receiptImages;

    @Data
    public static class ReceiptFiles {
    	private String fileName;
    	private String originalFileName;
    	private String productImage;
    }
}
