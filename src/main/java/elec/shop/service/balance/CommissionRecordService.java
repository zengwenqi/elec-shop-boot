package elec.shop.service.balance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.balance.CommissionRecord;
import elec.shop.pojo.balance.dto.CommissionQueryDTO;
import elec.shop.pojo.balance.vo.CommissionRecordVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author System
 * @description 针对表【commission_record(佣金记录表)】的数据库操作Service
 * @createDate 2025-01-16 00:00:00
 */
public interface CommissionRecordService extends IService<CommissionRecord> {
    
    /**
     * 分页查询佣金记录列表
     */
    IPage<CommissionRecordVO> getCommissionRecordPage(CommissionQueryDTO queryDTO);
    
    /**
     * 查询佣金记录列表
     */
    List<CommissionRecordVO> getCommissionRecordList(CommissionQueryDTO queryDTO);
    
    /**
     * 根据ID查询佣金记录详情
     */
    CommissionRecordVO getCommissionRecordById(Long recordId);
    
    /**
     * 根据订单ID查询佣金记录
     */
    List<CommissionRecord> getCommissionRecordByOrderId(Long orderId);
    
    /**
     * 根据用户ID查询佣金记录
     */
    List<CommissionRecordVO> getCommissionRecordByUserId(Long userId, Integer commissionStatus);
    
    /**
     * 创建佣金记录
     */
    Boolean createCommissionRecord(Long orderId, String orderNo, BigDecimal orderAmount, 
                                  Long userId, Integer userType);
    
    /**
     * 批量创建佣金记录
     */
    Boolean batchCreateCommissionRecord(List<Long> orderIds);
    
    /**
     * 发放佣金
     */
    Boolean payoutCommission(Long recordId);
    
    /**
     * 批量发放佣金
     */
    Boolean batchPayoutCommission(List<Long> recordIds);
    
    /**
     * 取消佣金
     */
    Boolean cancelCommission(Long recordId, String reason);
    
    /**
     * 批量取消佣金
     */
    Boolean batchCancelCommission(List<Long> recordIds, String reason);
    
    /**
     * 统计用户佣金总额
     */
    BigDecimal sumCommissionByUserId(Long userId, Integer commissionStatus, Date startTime, Date endTime);
    
    /**
     * 统计佣金记录数量
     */
    Long countCommissionRecord(Integer userType, Integer commissionStatus, Date startTime, Date endTime);
    
    /**
     * 计算订单佣金金额
     */
    BigDecimal calculateCommissionAmount(Long userId, Integer userType, BigDecimal orderAmount);
    
    /**
     * 生成佣金编号
     */
    String generateCommissionNo();
    
    /**
     * 校验佣金记录数据
     */
    void validateCommissionRecord(CommissionRecord record);
}