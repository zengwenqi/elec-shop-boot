package elec.shop.mapper.balance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.pojo.balance.CommissionRecord;
import elec.shop.pojo.balance.vo.CommissionRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author System
 * @description 针对表【commission_record(佣金记录表)】的数据库操作Mapper
 * @createDate 2025-01-16 00:00:00
 * @Entity elec.shop.pojo.balance.CommissionRecord
 */
@Mapper
public interface CommissionRecordMapper extends BaseMapper<CommissionRecord> {

    /**
     * 分页查询佣金记录列表
     */
    IPage<CommissionRecordVO> selectCommissionRecordPage(IPage<CommissionRecordVO> page,
                                                        @Param("keyword") String keyword,
                                                        @Param("userType") Integer userType,
                                                        @Param("commissionStatus") Integer commissionStatus,
                                                        @Param("startTime") String startTime,
                                                        @Param("endTime") String endTime,
                                                        @Param("settlementPeriod") String settlementPeriod);

    /**
     * 查询佣金记录列表
     */
    List<CommissionRecordVO> selectCommissionRecordList(@Param("keyword") String keyword,
                                                       @Param("userType") Integer userType,
                                                       @Param("commissionStatus") Integer commissionStatus,
                                                       @Param("startTime") String startTime,
                                                       @Param("endTime") String endTime,
                                                       @Param("settlementPeriod") String settlementPeriod);

    /**
     * 查询佣金记录详情
     */
    CommissionRecordVO selectCommissionRecordById(@Param("recordId") Long recordId);

    /**
     * 根据订单ID查询佣金记录
     */
    List<CommissionRecord> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据用户ID查询佣金记录
     */
    List<CommissionRecordVO> selectByUserId(@Param("userId") Long userId,
                                           @Param("commissionStatus") Integer commissionStatus);

    /**
     * 统计用户佣金总额
     */
    BigDecimal sumCommissionByUserId(@Param("userId") Long userId,
                                    @Param("commissionStatus") Integer commissionStatus,
                                    @Param("startTime") Date startTime,
                                    @Param("endTime") Date endTime);

    /**
     * 统计佣金记录数量
     */
    Long countCommissionRecord(@Param("userType") Integer userType,
                              @Param("commissionStatus") Integer commissionStatus,
                              @Param("startTime") Date startTime,
                              @Param("endTime") Date endTime);

    /**
     * 批量更新佣金状态
     */
    int batchUpdateStatus(@Param("recordIds") List<Long> recordIds,
                         @Param("status") Integer status,
                         @Param("payoutTime") Date payoutTime,
                         @Param("updatedBy") String updatedBy);

}
