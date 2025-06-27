package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.purchase.ShopInfo;
import elec.shop.pojo.purchase.dto.ShopInfoDTO;
import elec.shop.pojo.purchase.dto.ShopInfoQueryDTO;
import elec.shop.pojo.purchase.dto.PurchaserOrderQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaserOrderVO;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.service.purchase.ShopInfoService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "店铺管理")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopInfoService shopInfoService;
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/shopInfo")
    @ApiOperation("查询我的店铺信息")
    public Result<Object> queryShopInfo(@RequestBody ShopInfoQueryDTO shopInfoQueryDTO) {
        IPage<ShopInfo> shopInfoIPage = shopInfoService.queryShopInfo(shopInfoQueryDTO);
        return Result.ok(shopInfoIPage);
    }

    @PostMapping("/shopInfoList")
    @ApiOperation("查询我的所有店铺信息")
    public Result<Object> queryShopInfo() {
        List<ShopInfo> shopInfoList = shopInfoService.queryShopInfoList();
        return Result.ok(shopInfoList);
    }

    @PostMapping("/updateShopInfo")
    @ApiOperation("更新我的店铺信息")
    public Result<Object> updateShopInfo(@RequestBody ShopInfoDTO shopInfoDTO) {
        return shopInfoService.updateShopInfo(shopInfoDTO);
    }

    @PostMapping("/shopInfoStatistic")
    @ApiOperation("我的店铺信息采购订单统计")
    public Result<Object> shopInfoStatistic(@RequestParam("shopId") Long shopId) {
        Map<String, Object> statistics = shopInfoService.getShopOrderStatistics(shopId);
        return Result.ok(statistics);
    }

    @PostMapping("/shopInfoPurchaseOrder")
    @ApiOperation("查询我的店铺下的采购订单")
    public Result<Object> shopInfoPurchaseOrder(@RequestBody PurchaserOrderQueryDTO queryDTO) {
        if (queryDTO.getShopId() == null) {
            return Result.fail().message("店铺ID不能为空");
        }
        IPage<PurchaserOrderVO> orderPage = purchaseOrderService.queryUserOrders(queryDTO);
        return Result.ok(orderPage);
    }

    @PostMapping("/deleteShop")
    @ApiOperation("删除店铺")
    public Result<Object> deleteShop(@RequestParam("shopId") Long shopId) {
        return shopInfoService.deleteShop(shopId);
    }

    @PostMapping("/addShop")
    @ApiOperation("新增店铺")
    public Result<Object> addShop(@RequestBody ShopInfoDTO shopInfoDTO) {
        return shopInfoService.addShop(shopInfoDTO);
    }

}
