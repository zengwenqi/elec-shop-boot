package elec.shop.service.finance.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.finance.MoneyLogMapper;
import elec.shop.pojo.finance.MoneyLog;
import elec.shop.pojo.finance.dto.MoneyLogQueryDTO;
import elec.shop.service.finance.MoneyLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 财务日志服务实现类
 */
@Slf4j
@Service
public class MoneyLogServiceImpl extends ServiceImpl<MoneyLogMapper, MoneyLog> implements MoneyLogService {

    @Override
    public IPage<MoneyLog> getMoneyLogPage(MoneyLogQueryDTO queryDTO) {
        Page<MoneyLog> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return baseMapper.selectMoneyLogPage(page, queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordMoneyLog(Long userId, String username, Integer userType, String operationType,
                                 BigDecimal amount, String currency, String relatedOrderNo, Long relatedId,
                                 String description, String remark, Long operatorId, String operatorName,
                                 String ipAddress) {
        try {
            // 获取用户当前余额
            BigDecimal currentBalance = getLatestBalance(userId, currency);
            if (currentBalance == null) {
                currentBalance = BigDecimal.ZERO;
            }

            // 计算操作后余额
            BigDecimal afterBalance = currentBalance.add(amount);

            // 创建财务日志记录
            MoneyLog moneyLog = new MoneyLog()
                    .setUserId(userId)
                    .setUsername(username)
                    .setUserType(userType)
                    .setOperationType(operationType)
                    .setAmount(amount)
                    .setBalanceBefore(currentBalance)
                    .setBalanceAfter(afterBalance)
                    .setCurrency(currency)
                    .setRelatedOrderNo(relatedOrderNo)
                    .setRelatedId(relatedId)
                    .setDescription(description)
                    .setRemark(remark)
                    .setOperatorId(operatorId)
                    .setOperatorName(operatorName)
                    .setIpAddress(ipAddress)
                    .setStatus(MoneyLog.Status.SUCCESS.getCode())
                    .setCreatedAt(LocalDateTime.now())
                    .setUpdatedAt(LocalDateTime.now());

            boolean result = save(moneyLog);
            
            if (result) {
                log.info("财务日志记录成功: userId={}, operationType={}, amount={}, currency={}", 
                        userId, operationType, amount, currency);
            } else {
                log.error("财务日志记录失败: userId={}, operationType={}, amount={}, currency={}", 
                        userId, operationType, amount, currency);
            }
            
            return result;
        } catch (Exception e) {
            log.error("记录财务日志异常: userId={}, operationType={}, amount={}, currency={}", 
                    userId, operationType, amount, currency, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserBalanceStats(Long userId, String startTime, String endTime) {
        return baseMapper.selectUserBalanceStats(userId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getOperationTypeStats(String startTime, String endTime) {
        return baseMapper.selectOperationTypeStats(startTime, endTime);
    }

    @Override
    public BigDecimal getLatestBalance(Long userId, String currency) {
        BigDecimal balance = baseMapper.selectLatestBalance(userId, currency);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalAmountByDateRange(String startTime, String endTime, String operationType) {
        BigDecimal total = baseMapper.selectTotalAmountByDateRange(startTime, endTime, operationType);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<MoneyLog> exportMoneyLog(MoneyLogQueryDTO queryDTO) {
        // 设置导出时不分页，获取所有数据
        queryDTO.setPage(1);
        queryDTO.setSize(Integer.MAX_VALUE);
        
        IPage<MoneyLog> page = getMoneyLogPage(queryDTO);
        return page.getRecords();
    }

    /**
     * 记录充值日志
     */
    public boolean recordRecharge(Long userId, String username, Integer userType, BigDecimal amount, 
                                 String currency, String relatedOrderNo, String remark, 
                                 Long operatorId, String operatorName, String ipAddress) {
        return recordMoneyLog(userId, username, userType, MoneyLog.OperationType.RECHARGE.getCode(),
                amount, currency, relatedOrderNo, null, "账户充值", remark, 
                operatorId, operatorName, ipAddress);
    }

    /**
     * 记录采购支出日志
     */
    public boolean recordPurchase(Long userId, String username, Integer userType, BigDecimal amount, 
                                 String currency, String relatedOrderNo, Long relatedId, String remark, 
                                 String ipAddress) {
        return recordMoneyLog(userId, username, userType, MoneyLog.OperationType.PURCHASE.getCode(),
                amount.negate(), currency, relatedOrderNo, relatedId, "采购支出", remark, 
                null, null, ipAddress);
    }

    /**
     * 记录退款日志
     */
    public boolean recordRefund(Long userId, String username, Integer userType, BigDecimal amount, 
                               String currency, String relatedOrderNo, Long relatedId, String remark, 
                               Long operatorId, String operatorName, String ipAddress) {
        return recordMoneyLog(userId, username, userType, MoneyLog.OperationType.REFUND.getCode(),
                amount, currency, relatedOrderNo, relatedId, "订单退款", remark, 
                operatorId, operatorName, ipAddress);
    }

    /**
     * 记录佣金日志
     */
    public boolean recordCommission(Long userId, String username, Integer userType, BigDecimal amount, 
                                   String currency, String relatedOrderNo, Long relatedId, String remark, 
                                   String ipAddress) {
        return recordMoneyLog(userId, username, userType, MoneyLog.OperationType.COMMISSION.getCode(),
                amount, currency, relatedOrderNo, relatedId, "佣金收入", remark, 
                null, null, ipAddress);
    }

    /**
     * 记录提现日志
     */
    public boolean recordWithdraw(Long userId, String username, Integer userType, BigDecimal amount, 
                                 String currency, String relatedOrderNo, String remark, 
                                 String ipAddress) {
        return recordMoneyLog(userId, username, userType, MoneyLog.OperationType.WITHDRAW.getCode(),
                amount.negate(), currency, relatedOrderNo, null, "账户提现", remark, 
                null, null, ipAddress);
    }

    /**
     * 记录转账日志
     */
    public boolean recordTransfer(Long userId, String username, Integer userType, BigDecimal amount, 
                                 String currency, String relatedOrderNo, String remark, 
                                 Long operatorId, String operatorName, String ipAddress) {
        return recordMoneyLog(userId, username, userType, MoneyLog.OperationType.TRANSFER.getCode(),
                amount, currency, relatedOrderNo, null, "账户转账", remark, 
                operatorId, operatorName, ipAddress);
    }
}