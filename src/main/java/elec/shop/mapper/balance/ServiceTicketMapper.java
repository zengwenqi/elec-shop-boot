package elec.shop.mapper.balance;

import elec.shop.pojo.balance.ServiceTicket;
import elec.shop.pojo.balance.vo.ServiceTicketExportVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【service_ticket(工单表)】的数据库操作Mapper
* @createDate 2025-06-23 16:45:35
* @Entity elec.shop.pojo.balance.ServiceTicket
*/
public interface ServiceTicketMapper extends BaseMapper<ServiceTicket> {

    /**
     * 查询工单导出数据（多表关联查询）
     * @param userType 用户类型
     * @param userId 当前用户ID
     * @param purchaserId 采购员ID
     * @param queryType 查询类型
     * @param type 工单类型
     * @param typeInt 工单类型整数值
     * @param status 工单状态
     * @param statusInt 工单状态整数值
     * @param keyword 关键字
     * @param priority 优先级
     * @param filterUserId 过滤用户ID
     * @param filterPurchaseId 过滤采购员ID
     * @return 导出数据列表
     */
    List<ServiceTicketExportVO> selectTicketsForExport(
            @Param("userType") Integer userType,
            @Param("userId") Long userId,
            @Param("purchaserId") Long purchaserId,
            @Param("queryType") String queryType,
            @Param("type") String type,
            @Param("typeInt") Integer typeInt,
            @Param("status") String status,
            @Param("statusInt") Integer statusInt,
            @Param("keyword") String keyword,
            @Param("priority") Long priority,
            @Param("filterUserId") Long filterUserId,
            @Param("filterPurchaseId") Long filterPurchaseId
    );

}




