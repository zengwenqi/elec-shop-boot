package elec.shop.pojo.finance.dto;

import lombok.Data;

/**
 * 财务日志查询DTO
 */
@Data
public class MoneyLogQueryDTO {

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型：1-超级管理员，2-管理员，3-采购员，4-商户
     */
    private Integer userType;

    /**
     * 操作类型：RECHARGE-充值，PURCHASE-采购支出，REFUND-退款，COMMISSION-佣金，WITHDRAW-提现，TRANSFER-转账
     */
    private String operationType;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 关联订单号
     */
    private String relatedOrderNo;

    /**
     * 状态：1-成功，2-失败，3-处理中
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 最小金额
     */
    private String minAmount;

    /**
     * 最大金额
     */
    private String maxAmount;

    /**
     * 关键词搜索（用户名、描述、备注）
     */
    private String keyword;

    /**
     * 排序字段
     */
    private String sortField = "created_at";

    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String sortOrder = "desc";
}