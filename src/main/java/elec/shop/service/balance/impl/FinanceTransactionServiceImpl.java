package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.balance.FinanceTransaction;
import elec.shop.pojo.balance.dto.RechargeRequestDTO;
import elec.shop.pojo.balance.vo.RechargeRecordVO;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.balance.FinanceTransactionService;
import elec.shop.mapper.balance.FinanceTransactionMapper;
import elec.shop.utils.MinioUtil;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.service.purchase.AccountBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Lenovo
 * @description 针对表【finance_transaction(交易流水表)】的数据库操作Service实现
 * @createDate 2025-06-23 16:45:35
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceTransactionServiceImpl extends ServiceImpl<FinanceTransactionMapper, FinanceTransaction>
        implements FinanceTransactionService {

    // 常量定义
    private static final String TRANSACTION_NO_PREFIX = "RC";
    private static final int TRANSACTION_TYPE_RECHARGE = 1;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;
    private static final int STATUS_CANCELLED = 3;
    private static final int STATUS_APPEALING = 4;
    private static final String CURRENCY_CNY = "CNY";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String ADMIN_APPROVE_REMARK = " [管理员审核通过]";
    private static final String ADMIN_REJECT_REMARK = " [管理员审核拒绝：%s]";

    private final FinanceAccountMapper financeAccountMapper;
    private final AccountBalanceMapper accountBalanceMapper;
    private final MinioUtil minioUtil;
    private final SysUserMapper sysUserMapper;
    private final FinanceAccountService financeAccountService;
    private final AccountBalanceService accountBalanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRechargeRecord(Long userId, RechargeRequestDTO rechargeRequest) {
        validateCreateRechargeParams(userId, rechargeRequest);

        // 创建充值交易记录
        FinanceTransaction transaction = buildRechargeTransaction(userId, rechargeRequest);

        // 保存记录
        this.save(transaction);
        log.info("创建充值记录成功，用户ID: {}, 交易ID: {}", userId, transaction.getTransactionId());

        return transaction.getTransactionId();
    }

    @Override
    public IPage<RechargeRecordVO> getRechargeList(Long userId, String orderNo, String status,
                                                   String paymentMethod, String startTime, String endTime,
                                                   Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<FinanceTransaction> queryWrapper = buildRechargeListQuery(
                userId, orderNo, status, paymentMethod, startTime, endTime);

        // 分页查询
        Page<FinanceTransaction> pageParam = new Page<>(page, size);
        IPage<FinanceTransaction> pageResult = this.page(pageParam, queryWrapper);

        // 转换为VO
        return convertToRechargeRecordVOPage(pageResult, userId);
    }

    @Override
    public RechargeRecordVO getRechargeDetail(Long userId, String transactionId) {
        FinanceTransaction transaction = this.lambdaQuery()
                .eq(FinanceTransaction::getTransactionId, transactionId)
                .eq(FinanceTransaction::getAccountId, userId)
                .eq(FinanceTransaction::getTransactionType, TRANSACTION_TYPE_RECHARGE)
                .one();

        return transaction != null ? convertToVO(transaction) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRecharge(Long userId, String transactionId) {
        FinanceTransaction transaction = getRechargeTransaction(userId, transactionId);
        if (transaction == null) {
            log.warn("取消充值失败，交易记录不存在，用户ID: {}, 交易ID: {}", userId, transactionId);
            return false;
        }

        // 只有待审核状态的充值可以取消
        if (transaction.getStatus() != STATUS_PENDING) {
            log.warn("取消充值失败，状态不允许取消，用户ID: {}, 交易ID: {}, 当前状态: {}",
                    userId, transactionId, transaction.getStatus());
            return false;
        }

        // 更新状态为已取消
        transaction.setStatus(STATUS_CANCELLED);
        transaction.setUpdatedAt(new Date());
        boolean result = this.updateById(transaction);

        if (result) {
            log.info("取消充值成功，用户ID: {}, 交易ID: {}", userId, transactionId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean appealRecharge(Long userId, String transactionId, String type, String description, String contact) {
        FinanceTransaction transaction = getRechargeTransaction(userId, transactionId);
        if (transaction == null) {
            log.warn("申诉充值失败，交易记录不存在，用户ID: {}, 交易ID: {}", userId, transactionId);
            return false;
        }

        // 只有失败或已取消状态的充值可以申诉
        if (transaction.getStatus() != STATUS_FAILED && transaction.getStatus() != STATUS_CANCELLED) {
            log.warn("申诉充值失败，状态不允许申诉，用户ID: {}, 交易ID: {}, 当前状态: {}",
                    userId, transactionId, transaction.getStatus());
            return false;
        }

        // 更新状态为申诉中
        transaction.setStatus(STATUS_APPEALING);
        transaction.setUpdatedAt(new Date());
        boolean result = this.updateById(transaction);

        if (result) {
            log.info("申诉充值成功，用户ID: {}, 交易ID: {}", userId, transactionId);
            // TODO: 创建申诉记录到专门的申诉表
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRecharge(String transactionId, Long adminUserId,String remark) {
        FinanceTransaction transaction = getTransactionById(transactionId);
        if (transaction == null) {
            log.warn("审核通过失败，交易记录不存在，交易ID: {}", transactionId);
            return false;
        }

        // 只有待审核状态的充值可以审核通过
        if (transaction.getStatus() != STATUS_PENDING) {
            log.warn("审核通过失败，状态不允许审核，交易ID: {}, 当前状态: {}",
                    transactionId, transaction.getStatus());
            return false;
        }

        try {
            // 更新交易状态为成功
            transaction.setStatus(STATUS_SUCCESS);
            transaction.setUpdatedAt(new Date());
            transaction.setRemark(transaction.getRemark() + ADMIN_APPROVE_REMARK + remark);

            // 增加用户余额
            processRechargeBalance(transaction);

            boolean result = this.updateById(transaction);
            if (result) {
                log.info("审核通过成功，交易ID: {}, 管理员ID: {}", transactionId, adminUserId);
            }
            return result;
        } catch (Exception e) {
            log.error("审核通过失败，交易ID: {}, 错误: {}", transactionId, e.getMessage(), e);
            throw new RuntimeException("审核通过失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectRecharge(String transactionId, Long adminUserId, String reason) {
        FinanceTransaction transaction = getTransactionById(transactionId);
        if (transaction == null) {
            log.warn("审核拒绝失败，交易记录不存在，交易ID: {}", transactionId);
            return false;
        }

        // 只有待审核状态的充值可以审核拒绝
        if (transaction.getStatus() != STATUS_PENDING) {
            log.warn("审核拒绝失败，状态不允许审核，交易ID: {}, 当前状态: {}",
                    transactionId, transaction.getStatus());
            return false;
        }

        // 更新交易状态为失败
        transaction.setStatus(STATUS_FAILED);
        transaction.setUpdatedAt(new Date());
        transaction.setRemark(transaction.getRemark() + String.format(ADMIN_REJECT_REMARK, reason));

        boolean result = this.updateById(transaction);
        if (result) {
            log.info("审核拒绝成功，交易ID: {}, 管理员ID: {}, 拒绝原因: {}", transactionId, adminUserId, reason);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchApproveRecharge(List<String> transactionIds, Long adminUserId) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            log.warn("批量审核通过失败，交易ID列表为空");
            return 0;
        }

        int successCount = 0;
        for (String transactionId : transactionIds) {
            try {
                if (approveRecharge(transactionId, adminUserId,null)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量审核通过失败，交易ID: {}, 错误: {}", transactionId, e.getMessage());
            }
        }

        log.info("批量审核通过完成，总数: {}, 成功: {}, 管理员ID: {}",
                transactionIds.size(), successCount, adminUserId);
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRejectRecharge(List<String> transactionIds, Long adminUserId, String reason) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            log.warn("批量审核拒绝失败，交易ID列表为空");
            return 0;
        }

        int successCount = 0;
        for (String transactionId : transactionIds) {
            try {
                if (rejectRecharge(transactionId, adminUserId, reason)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量审核拒绝失败，交易ID: {}, 错误: {}", transactionId, e.getMessage());
            }
        }

        log.info("批量审核拒绝完成，总数: {}, 成功: {}, 管理员ID: {}",
                transactionIds.size(), successCount, adminUserId);
        return successCount;
    }

    @Override
    public IPage<RechargeRecordVO> getPendingRechargeList(String orderNo, String paymentMethod,
                                                          String startTime, String endTime,
                                                          Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<FinanceTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FinanceTransaction::getTransactionType, TRANSACTION_TYPE_RECHARGE)
                .eq(FinanceTransaction::getStatus, STATUS_PENDING);

        // 添加查询条件
        addCommonQueryConditions(queryWrapper, orderNo, paymentMethod, startTime, endTime);

        // 按创建时间倒序
        queryWrapper.orderByDesc(FinanceTransaction::getCreatedAt);

        // 分页查询
        Page<FinanceTransaction> pageParam = new Page<>(page, size);
        IPage<FinanceTransaction> pageResult = this.page(pageParam, queryWrapper);

        // 转换为VO并添加用户信息
        IPage<RechargeRecordVO> voPage = new Page<>(page, size, pageResult.getTotal());
        List<RechargeRecordVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVOWithUserInfo)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    // ==================== 私有方法 ====================

    /**
     * 参数校验
     */
    private void validateCreateRechargeParams(Long userId, RechargeRequestDTO rechargeRequest) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (rechargeRequest == null) {
            throw new IllegalArgumentException("充值请求参数不能为空");
        }
        if (rechargeRequest.getAmount() == null || rechargeRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        if (!StringUtils.hasText(rechargeRequest.getCurrency())) {
            throw new IllegalArgumentException("币种不能为空");
        }
        if (rechargeRequest.getPaymentImages() == null || rechargeRequest.getPaymentImages().isEmpty()) {
            throw new IllegalArgumentException("支付凭证不能为空");
        }
    }

    /**
     * 构建充值交易记录
     */
    private FinanceTransaction buildRechargeTransaction(Long userId, RechargeRequestDTO rechargeRequest) {
        FinanceTransaction transaction = new FinanceTransaction();

        // 生成交易流水号
        String transactionNo = generateTransactionNo();
        transaction.setTransactionNo(transactionNo);

        // 设置基本信息
        transaction.setAccountId(userId);
        transaction.setTransactionType(TRANSACTION_TYPE_RECHARGE);
        transaction.setAmount(rechargeRequest.getAmount());
        transaction.setCurrency(rechargeRequest.getCurrency());
        transaction.setStatus(STATUS_PENDING);
        transaction.setPaymentMethod(rechargeRequest.getPaymentMethod());
        transaction.setRemark(rechargeRequest.getRemark());
        transaction.setRealPaymentMoney(rechargeRequest.getRealPaymentMoney());

        // 处理支付凭证图片
        transaction.setPaymentImage(String.join(",", rechargeRequest.getPaymentImages()));

        // 设置关联账户和汇率
        setRelatedAccountAndExchangeRate(transaction, userId, rechargeRequest.getCurrency());

        return transaction;
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        return TRANSACTION_NO_PREFIX + System.currentTimeMillis() +
               String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 设置关联账户和汇率
     */
    private void setRelatedAccountAndExchangeRate(FinanceTransaction transaction, Long userId, String currency) {
        // 获取用户对应的财务账户
        FinanceAccount financeAccount = financeAccountMapper.selectOne(
                new LambdaQueryWrapper<FinanceAccount>().eq(FinanceAccount::getUserId, userId));

        if (financeAccount == null) {
            throw new RuntimeException("用户财务账户不存在，用户ID: " + userId);
        }

        transaction.setRelatedAccountId(financeAccount.getAccountId());

        if (CURRENCY_CNY.equals(currency)) {
            transaction.setExchangeRate(BigDecimal.ONE);
        } else {
            AccountBalance accountBalance = accountBalanceMapper.selectOne(
                    new LambdaQueryWrapper<AccountBalance>()
                            .eq(AccountBalance::getAccountId, financeAccount.getAccountId())
                            .eq(AccountBalance::getCurrency, currency));

            if (accountBalance == null) {
                throw new RuntimeException("账户余额记录不存在，账户ID: " + financeAccount.getAccountId() + ", 币种: " + currency);
            }

            transaction.setExchangeRate(accountBalance.getExchangeRate());
        }
    }

    /**
     * 构建充值列表查询条件
     */
    private LambdaQueryWrapper<FinanceTransaction> buildRechargeListQuery(Long userId, String orderNo,
                                                                          String status, String paymentMethod,
                                                                          String startTime, String endTime) {
        LambdaQueryWrapper<FinanceTransaction> queryWrapper = new LambdaQueryWrapper<>();

        // 判断是否为管理员
        SysUser sysUser = sysUserMapper.selectById(userId);
        boolean isAdmin = sysUser != null && sysUser.getUserType() == 1;

        if (!isAdmin) {
            // 普通用户只能查看自己的充值记录
            queryWrapper.eq(FinanceTransaction::getAccountId, userId);
        }

        queryWrapper.eq(FinanceTransaction::getTransactionType, TRANSACTION_TYPE_RECHARGE);

        // 添加通用查询条件
        addCommonQueryConditions(queryWrapper, orderNo, paymentMethod, startTime, endTime);

        // 添加状态条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(FinanceTransaction::getStatus, getStatusCode(status));
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(FinanceTransaction::getCreatedAt);

        return queryWrapper;
    }

    /**
     * 添加通用查询条件
     */
    private void addCommonQueryConditions(LambdaQueryWrapper<FinanceTransaction> queryWrapper,
                                          String orderNo, String paymentMethod,
                                          String startTime, String endTime) {
        if (StringUtils.hasText(orderNo)) {
            queryWrapper.like(FinanceTransaction::getTransactionNo, orderNo);
        }
        if (StringUtils.hasText(paymentMethod)) {
            queryWrapper.eq(FinanceTransaction::getPaymentMethod, paymentMethod);
        }
        if (StringUtils.hasText(startTime)) {
            queryWrapper.ge(FinanceTransaction::getCreatedAt, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            queryWrapper.le(FinanceTransaction::getCreatedAt, endTime);
        }
    }

    /**
     * 转换为充值记录VO分页对象
     */
    private IPage<RechargeRecordVO> convertToRechargeRecordVOPage(IPage<FinanceTransaction> pageResult, Long userId) {
        // 判断是否为管理员
        SysUser sysUser = sysUserMapper.selectById(userId);
        boolean isAdmin = sysUser != null && sysUser.getUserType() == 1;

        IPage<RechargeRecordVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<RechargeRecordVO> voList;

        if (isAdmin) {
            // 管理员视图：包含用户信息
            voList = pageResult.getRecords().stream()
                    .map(this::convertToVOWithUserInfo)
                    .toList();
        } else {
            // 普通用户视图：不包含用户信息
            voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .toList();
        }

        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取充值交易记录
     */
    private FinanceTransaction getRechargeTransaction(Long userId, String transactionId) {
        return this.lambdaQuery()
                .eq(FinanceTransaction::getTransactionId, transactionId)
                .eq(FinanceTransaction::getAccountId, userId)
                .eq(FinanceTransaction::getTransactionType, TRANSACTION_TYPE_RECHARGE)
                .one();
    }

    /**
     * 根据交易ID获取交易记录
     */
    private FinanceTransaction getTransactionById(String transactionId) {
        return this.lambdaQuery()
                .eq(FinanceTransaction::getTransactionNo, transactionId)
                .eq(FinanceTransaction::getTransactionType, TRANSACTION_TYPE_RECHARGE)
                .one();
    }

    /**
     * 处理充值余额增加
     */
    private void processRechargeBalance(FinanceTransaction transaction) {
        if (CURRENCY_CNY.equals(transaction.getCurrency())) {
            // 人民币充值，直接增加基础账户余额
            financeAccountService.rechargeCNY(transaction.getAccountId(), transaction.getAmount());
        } else {
            // 外币充值，增加对应币种余额
            accountBalanceService.rechargeForex(
                    String.valueOf(transaction.getRelatedAccountId()),
                    transaction.getCurrency(),
                    transaction.getAmount()
            );
        }
    }

    /**
     * 转换为VO对象
     */
    private RechargeRecordVO convertToVO(FinanceTransaction transaction) {
        RechargeRecordVO vo = new RechargeRecordVO();
        BeanUtils.copyProperties(transaction, vo);

        vo.setTransactionId(String.valueOf(transaction.getTransactionId()));
        vo.setOrderNo(transaction.getTransactionNo());
        vo.setPayMethod(transaction.getPaymentMethod());
        vo.setStatus(getStatusText(transaction.getStatus()));

        // 处理支付凭证图片
        if (StringUtils.hasText(transaction.getPaymentImage())) {
            String[] imageArray = transaction.getPaymentImage().split(",");
            List<String> paymentImages = Arrays.stream(imageArray)
                    .map(minioUtil::getPreviewUrl)
                    .toList();
            vo.setPaymentImages(paymentImages);
        } else {
            vo.setPaymentImages(new ArrayList<>());
        }

        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        if (transaction.getCreatedAt() != null) {
            vo.setCreateTime(sdf.format(transaction.getCreatedAt()));
        }
        vo.setUpdateTime(transaction.getUpdatedAt() != null ?
                sdf.format(transaction.getUpdatedAt()) : null);

        // 设置完成时间（成功状态时使用更新时间）
        if (transaction.getStatus() == STATUS_SUCCESS && transaction.getUpdatedAt() != null) {
            vo.setCompleteTime(sdf.format(transaction.getUpdatedAt()));
        }

        return vo;
    }

    /**
     * 转换为VO对象并包含用户信息（管理员视图）
     */
    private RechargeRecordVO convertToVOWithUserInfo(FinanceTransaction transaction) {
        RechargeRecordVO vo = convertToVO(transaction);

        // 添加用户信息
        SysUser user = sysUserMapper.selectById(transaction.getAccountId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserId(String.valueOf(user.getUserId()));
        }

        return vo;
    }

    /**
     * 状态文本转换为状态码
     */
    private Integer getStatusCode(String statusText) {
        return switch (statusText) {
            case "待支付", "处理中", "待审核" -> STATUS_PENDING;
            case "成功", "已完成" -> STATUS_SUCCESS;
            case "失败" -> STATUS_FAILED;
            case "已取消" -> STATUS_CANCELLED;
            case "申诉中" -> STATUS_APPEALING;
            default -> null;
        };
    }

    /**
     * 状态码转换为状态文本
     */
    private String getStatusText(Integer status) {
        return switch (status) {
            case STATUS_PENDING -> "待审核";
            case STATUS_SUCCESS -> "成功";
            case STATUS_FAILED -> "失败";
            case STATUS_CANCELLED -> "已取消";
            case STATUS_APPEALING -> "申诉中";
            default -> "未知";
        };
    }
}




