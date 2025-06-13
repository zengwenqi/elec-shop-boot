package elec.shop.mapper.purchase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.pojo.purchase.PurchaseOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import elec.shop.pojo.purchase.dto.PurchaseOrderQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaseOrderExportVO;
import elec.shop.pojo.purchase.vo.PurchaseOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【purchase_order(采购订单主表)】的数据库操作Mapper
* @createDate 2025-06-05 11:28:32
* @Entity elec.shop.pojo.purchase.PurchaseOrder
*/
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    /**
     * 查询导出订单数据
     * @param userId 用户ID
     * @param shopId 店铺ID，可选
     * @param startTime 开始时间，可选
     * @param endTime 结束时间，可选
     * @return 订单导出数据列表
     */
    List<PurchaseOrderExportVO> selectExportOrders(
            @Param("userId") Long userId,
            @Param("shopId") Long shopId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );

    /**
     * 分页查询采购订单列表
     */
    IPage<PurchaseOrderVO> queryPurchaseOrderList(IPage<PurchaseOrderVO> page,
                                                  @Param("queryDTO") PurchaseOrderQueryDTO queryDTO);

    /**
     * 查询采购订单列表（不分页）
     */
    List<PurchaseOrderVO> queryPurchaseOrderList(@Param("queryDTO") PurchaseOrderQueryDTO queryDTO);

    /**
     * 查询采购订单详情
     */
    PurchaseOrderVO queryPurchaseOrderOne(@Param("orderId") Long orderId);
}




