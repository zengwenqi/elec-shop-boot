package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.balance.CommissionRecordMapper;
import elec.shop.pojo.balance.CommissionRecord;
import elec.shop.pojo.balance.GlobalCommissionConfig;
import elec.shop.pojo.balance.UserCommissionConfig;
import elec.shop.pojo.balance.dto.CommissionQueryDTO;
import elec.shop.pojo.balance.enums.CommissionStatusEnum;
import elec.shop.pojo.balance.enums.CommissionTypeEnum;
import elec.shop.pojo.balance.vo.CommissionRecordVO;
import elec.shop.service.balance.CommissionRecordService;
import elec.shop.service.balance.GlobalCommissionConfigService;
import elec.shop.service.balance.UserCommissionConfigService;
import elec.shop.utils.AllContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author System
 * @description 针对表【commission_record(佣金记录表)】的数据库操作Service实现
 * @createDate 2025-01-16 00:00:00
 */
@Slf4j
@Service
public class CommissionRecordServiceImpl extends ServiceImpl<CommissionRecordMapper, CommissionRecord>
        implements CommissionRecordService {

    @Autowired
    private UserCommissionConfigService userCommissionConfigService;

    @Autowired
    private GlobalCommissionConfigService globalCommissionConfigService;

    @Override
    public IPage<CommissionRecordVO> getCommissionRecordPage(CommissionQueryDTO queryDTO) {
        Page<CommissionRecordVO> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return baseMapper.selectCommissionRecordPage(page, queryDTO.getKeyword(), queryDTO.getUserType(),
                queryDTO.getCommissionStatus(), queryDTO.getStartTime(), queryDTO.getEndTime(),
                queryDTO.getSettlementPeriod());
    }

    @Override
    public List<CommissionRecordVO> getCommissionRecordList(CommissionQueryDTO queryDTO) {
        return baseMapper.selectCommissionRecordList(queryDTO.getKeyword(), queryDTO.getUserType(),
                queryDTO.getCommissionStatus(), queryDTO.getStartTime(), queryDTO.getEndTime(),
                queryDTO.getSettlementPeriod());
    }

    @Override
    public CommissionRecordVO getCommissionRecordById(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("记录ID不能为空");
        }
        return baseMapper.selectCommissionRecordById(recordId);
    }

    @Override
    public List<CommissionRecord> getCommissionRecordByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return baseMapper.selectByOrderId(orderId);
    }

    @Override
    public List<CommissionRecordVO> getCommissionRecordByUserId(Long userId, Integer commissionStatus) {
        if (userId == null) {
            return null;
        }
        return baseMapper.selectByUserId(userId, commissionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createCommissionRecord(Long orderId, String orderNo, BigDecimal orderAmount,
                                         Long userId, Integer userType) {
        if (orderId == null || !StringUtils.hasText(orderNo) || orderAmount == null
            || userId == null || userType == null) {
            throw new BusinessException("创建佣金记录参数不完整");
        }

        // 检查是否已存在该订单的佣金记录
        List<CommissionRecord> existRecords = getCommissionRecordByOrderId(orderId);
        if (!CollectionUtils.isEmpty(existRecords)) {
            log.warn("订单{}已存在佣金记录，跳过创建", orderId);
            return true;
        }

        // 计算佣金金额
        BigDecimal commissionAmount = calculateCommissionAmount(userId, userType, orderAmount);
        if (commissionAmount == null || commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("用户{}的佣金金额为0或无效，跳过创建佣金记录", userId);
            return true;
        }

        // 获取佣金配置信息
        UserCommissionConfig userConfig = userCommissionConfigService.getEffectiveConfigByUserId(userId);
        GlobalCommissionConfig globalConfig = null;
        Integer commissionType = null;
        BigDecimal commissionRate = null;

        if (userConfig != null) {
            commissionType = userConfig.getCommissionType();
            commissionRate = userConfig.getCommissionValue();
        } else {
            globalConfig = globalCommissionConfigService.getEffectiveConfigByUserType(userType);
            if (globalConfig != null) {
                commissionType = globalConfig.getCommissionType();
                commissionRate = globalConfig.getCommissionValue();
            }
        }

        // 创建佣金记录
        CommissionRecord record = new CommissionRecord();
        record.setCommissionNo(generateCommissionNo());
        record.setUserId(userId);
        record.setUserType(userType);
        record.setOrderId(orderId);
        record.setOrderNo(orderNo);
        record.setOrderAmount(orderAmount);
        record.setCommissionType(commissionType);
        record.setCommissionRate(commissionRate);
        record.setCommissionAmount(commissionAmount);
        record.setCommissionStatus(CommissionStatusEnum.PENDING.getCode());
        record.setSettlementPeriod(generateSettlementPeriod());
        record.setCreatedBy(AllContextUtils.getLoginSysUser().getUsername());
        record.setCreatedAt(new Date());

        // 校验数据
        validateCommissionRecord(record);

        return this.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchCreateCommissionRecord(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            throw new BusinessException("订单ID列表不能为空");
        }

        // TODO: 根据订单ID查询订单信息，然后批量创建佣金记录
        // 这里需要调用订单服务获取订单详情
        log.info("批量创建佣金记录，订单数量：{}", orderIds.size());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payoutCommission(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("记录ID不能为空");
        }

        CommissionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("佣金记录不存在");
        }

        if (!CommissionStatusEnum.PENDING.getCode().equals(record.getCommissionStatus())) {
            throw new BusinessException("只能发放待发放状态的佣金");
        }

        record.setCommissionStatus(CommissionStatusEnum.PAID.getCode());
        record.setPayoutTime(new Date());
        record.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        record.setUpdatedAt(new Date());

        return this.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchPayoutCommission(List<Long> recordIds) {
        if (CollectionUtils.isEmpty(recordIds)) {
            throw new BusinessException("记录ID列表不能为空");
        }

        Date payoutTime = new Date();
        String updatedBy = AllContextUtils.getLoginSysUser().getUsername();

        return baseMapper.batchUpdateStatus(recordIds, CommissionStatusEnum.PAID.getCode(),
                payoutTime, updatedBy) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelCommission(Long recordId, String reason) {
        if (recordId == null) {
            throw new BusinessException("记录ID不能为空");
        }

        CommissionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("佣金记录不存在");
        }

        if (CommissionStatusEnum.CANCELLED.getCode().equals(record.getCommissionStatus())) {
            throw new BusinessException("佣金已经是取消状态");
        }

        record.setCommissionStatus(CommissionStatusEnum.CANCELLED.getCode());
        if (StringUtils.hasText(reason)) {
            record.setRemark(record.getRemark() + "；取消原因：" + reason);
        }
        record.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        record.setUpdatedAt(new Date());

        return this.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchCancelCommission(List<Long> recordIds, String reason) {
        if (CollectionUtils.isEmpty(recordIds)) {
            throw new BusinessException("记录ID列表不能为空");
        }

        for (Long recordId : recordIds) {
            cancelCommission(recordId, reason);
        }

        return true;
    }

    @Override
    public BigDecimal sumCommissionByUserId(Long userId, Integer commissionStatus, Date startTime, Date endTime) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = baseMapper.sumCommissionByUserId(userId, commissionStatus, startTime, endTime);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public Long countCommissionRecord(Integer userType, Integer commissionStatus, Date startTime, Date endTime) {
        Long count = baseMapper.countCommissionRecord(userType, commissionStatus, startTime, endTime);
        return count != null ? count : 0L;
    }

    @Override
    public BigDecimal calculateCommissionAmount(Long userId, Integer userType, BigDecimal orderAmount) {
        if (userId == null || userType == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 优先查找用户专属配置
        UserCommissionConfig userConfig = userCommissionConfigService.getEffectiveConfigByUserId(userId);
        if (userConfig != null) {
            return calculateCommissionByConfig(orderAmount, userConfig.getCommissionType(),
                    userConfig.getCommissionValue(), userConfig.getMinCommission(),
                    userConfig.getMaxCommission());
        }

        // 查找全局配置
        GlobalCommissionConfig globalConfig = globalCommissionConfigService.getEffectiveConfigByUserType(userType);
        if (globalConfig != null) {
            return calculateCommissionByConfig(orderAmount, globalConfig.getCommissionType(),
                    globalConfig.getCommissionValue(), globalConfig.getMinCommission(),
                    globalConfig.getMaxCommission());
        }

        return BigDecimal.ZERO;
    }

    /**
     * 根据配置计算佣金金额
     */
    private BigDecimal calculateCommissionByConfig(BigDecimal orderAmount, Integer commissionType,
                                                  BigDecimal commissionValue, BigDecimal minAmount, BigDecimal maxAmount) {
        BigDecimal commission;

        if (CommissionTypeEnum.FIXED_AMOUNT.getCode().equals(commissionType)) {
            // 固定金额
            commission = commissionValue;
        } else if (CommissionTypeEnum.PERCENTAGE.getCode().equals(commissionType)) {
            // 百分比
            commission = orderAmount.multiply(commissionValue).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }

        // 应用最小和最大限制
        if (minAmount != null && commission.compareTo(minAmount) < 0) {
            commission = minAmount;
        }
        if (maxAmount != null && commission.compareTo(maxAmount) > 0) {
            commission = maxAmount;
        }

        return commission;
    }

    @Override
    public String generateCommissionNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String random = String.valueOf((int) (Math.random() * 1000));
        return "COMM" + timestamp + String.format("%03d", Integer.parseInt(random));
    }

    /**
     * 生成结算周期
     */
    private String generateSettlementPeriod() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(new Date());
    }

    @Override
    public void validateCommissionRecord(CommissionRecord record) {
        if (record == null) {
            throw new BusinessException("佣金记录数据不能为空");
        }

        if (!StringUtils.hasText(record.getCommissionNo())) {
            throw new BusinessException("佣金编号不能为空");
        }

        if (record.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        if (record.getUserType() == null) {
            throw new BusinessException("用户类型不能为空");
        }

        if (record.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }

        if (!StringUtils.hasText(record.getOrderNo())) {
            throw new BusinessException("订单号不能为空");
        }

        if (record.getOrderAmount() == null || record.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("订单金额必须大于0");
        }

        if (record.getCommissionAmount() == null || record.getCommissionAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("佣金金额不能小于0");
        }

        if (record.getCommissionStatus() == null) {
            throw new BusinessException("佣金状态不能为空");
        }

        if (CommissionStatusEnum.getByCode(record.getCommissionStatus()) == null) {
            throw new BusinessException("佣金状态无效");
        }
    }
}
