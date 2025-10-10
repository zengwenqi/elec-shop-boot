package elec.shop.mapper.finance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.pojo.finance.MoneyLog;
import elec.shop.pojo.finance.dto.MoneyLogQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务日志Mapper接口
 */
@Mapper
public interface MoneyLogMapper extends BaseMapper<MoneyLog> {

    /**
     * 分页查询财务日志
     *
     * @param page 分页对象
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<MoneyLog> selectMoneyLogPage(Page<MoneyLog> page, @Param("query") MoneyLogQueryDTO queryDTO);

    /**
     * 查询用户余额变动统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> selectUserBalanceStats(@Param("userId") Long userId, 
                                               @Param("startTime") String startTime, 
                                               @Param("endTime") String endTime);

    /**
     * 查询操作类型统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectOperationTypeStats(@Param("startTime") String startTime, 
                                                        @Param("endTime") String endTime);

    /**
     * 查询用户最新余额
     *
     * @param userId 用户ID
     * @param currency 货币类型
     * @return 最新余额
     */
    BigDecimal selectLatestBalance(@Param("userId") Long userId, @Param("currency") String currency);

    /**
     * 查询日期范围内的交易总额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operationType 操作类型
     * @return 交易总额
     */
    BigDecimal selectTotalAmountByDateRange(@Param("startTime") String startTime, 
                                            @Param("endTime") String endTime,
                                            @Param("operationType") String operationType);
}