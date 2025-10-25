package elec.shop.service.balance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.balance.FinanceTransaction;
import elec.shop.pojo.balance.dto.RechargeRequestDTO;
import elec.shop.pojo.balance.vo.RechargeRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【finance_transaction(交易流水表)】的数据库操作Service
* @createDate 2025-06-23 16:45:35
*/
public interface FinanceTransactionService extends IService<FinanceTransaction> {

    /**
     * 创建充值记录
     * @param userId 用户ID
     * @param rechargeRequest 充值请求
     * @return 充值记录ID
     */
    Long createRechargeRecord(Long userId, RechargeRequestDTO rechargeRequest);

    /**
     * 分页查询充值记录列表
     * @param userId 用户ID
     * @param orderNo 订单号
     * @param status 状态
     * @param paymentMethod 支付方式
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<RechargeRecordVO> getRechargeList(Long userId, String orderNo, String status,
                                           String paymentMethod, String startTime, String endTime,
                                           Integer page, Integer size);

    /**
     * 获取充值记录详情
     * @param userId 用户ID
     * @param transactionId 交易ID
     * @return 充值记录详情
     */
    RechargeRecordVO getRechargeDetail(Long userId, String transactionId);

    /**
     * 取消充值申请
     * @param userId 用户ID
     * @param transactionId 交易ID
     * @return 是否成功
     */
    boolean cancelRecharge(Long userId, String transactionId);

    /**
     * 申诉充值
     * @param userId 用户ID
     * @param transactionId 交易ID
     * @param type 申诉类型
     * @param description 申诉描述
     * @param contact 联系方式
     * @return 是否成功
     */
    boolean appealRecharge(Long userId, String transactionId, String type, String description, String contact);

    /**
     * 管理员审核通过充值
     * @param transactionId 交易ID
     * @param adminUserId 管理员用户ID
     * @return 是否成功
     */
    boolean approveRecharge(String transactionId, Long adminUserId,String remark);

    /**
     * 管理员审核拒绝充值
     * @param transactionId 交易ID
     * @param adminUserId 管理员用户ID
     * @param reason 拒绝原因
     * @return 是否成功
     */
    boolean rejectRecharge(String transactionId, Long adminUserId, String reason);

    /**
     * 管理员批量审核通过充值
     * @param transactionIds 交易ID列表
     * @param adminUserId 管理员用户ID
     * @return 成功处理的数量
     */
    int batchApproveRecharge(List<String> transactionIds, Long adminUserId);

    /**
     * 管理员批量审核拒绝充值
     * @param transactionIds 交易ID列表
     * @param adminUserId 管理员用户ID
     * @param reason 拒绝原因
     * @return 成功处理的数量
     */
    int batchRejectRecharge(List<String> transactionIds, Long adminUserId, String reason);

    /**
     * 获取待审核充值列表
     * @param orderNo 订单号
     * @param paymentMethod 支付方式
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<RechargeRecordVO> getPendingRechargeList(String orderNo, String paymentMethod,
                                                  String startTime, String endTime,
                                                  Integer page, Integer size);
}
