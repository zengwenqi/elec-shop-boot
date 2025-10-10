package elec.shop.service.finance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.finance.MoneyLog;
import elec.shop.pojo.finance.dto.MoneyLogQueryDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务日志服务接口
 */
public interface MoneyLogService extends IService<MoneyLog> {

    /**
     * 分页查询财务日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<MoneyLog> getMoneyLogPage(MoneyLogQueryDTO queryDTO);

    /**
     * 记录财务日志
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param operationType 操作类型
     * @param amount 变动金额
     * @param currency 货币类型
     * @param relatedOrderNo 关联订单号
     * @param relatedId 关联业务ID
     * @param description 操作描述
     * @param remark 备注
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @param ipAddress IP地址
     * @return 是否成功
     */
    boolean recordMoneyLog(Long userId, String username, Integer userType, String operationType,
                          BigDecimal amount, String currency, String relatedOrderNo, Long relatedId,
                          String description, String remark, Long operatorId, String operatorName,
                          String ipAddress);

    /**
     * 查询用户余额变动统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getUserBalanceStats(Long userId, String startTime, String endTime);

    /**
     * 查询操作类型统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> getOperationTypeStats(String startTime, String endTime);

    /**
     * 查询用户最新余额
     *
     * @param userId 用户ID
     * @param currency 货币类型
     * @return 最新余额
     */
    BigDecimal getLatestBalance(Long userId, String currency);

    /**
     * 查询日期范围内的交易总额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operationType 操作类型
     * @return 交易总额
     */
    BigDecimal getTotalAmountByDateRange(String startTime, String endTime, String operationType);

    /**
     * 导出财务日志
     *
     * @param queryDTO 查询条件
     * @return 导出数据
     */
    List<MoneyLog> exportMoneyLog(MoneyLogQueryDTO queryDTO);
}