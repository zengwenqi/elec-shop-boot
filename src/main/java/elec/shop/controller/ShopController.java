package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.annotation.OperationLog;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.pojo.purchase.ShopInfo;
import elec.shop.pojo.purchase.dto.PurchaseOrderQueryDTO;
import elec.shop.pojo.purchase.dto.ShopInfoDTO;
import elec.shop.pojo.purchase.dto.ShopInfoQueryDTO;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.service.purchase.ShopInfoService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "店铺管理")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopInfoService shopInfoService;
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/shopInfo")
    @ApiOperation("查询我的店铺信息")
    public Result queryShopInfo(@RequestBody ShopInfoQueryDTO shopInfoQueryDTO) {
        IPage<ShopInfo> shopInfoIPage = shopInfoService.queryShopInfo(shopInfoQueryDTO);
        return Result.ok(shopInfoIPage);
    }

    @PostMapping("/updateShopInfo")
    @ApiOperation("更新我的店铺信息")
    public Result updateShopInfo(@RequestBody ShopInfoDTO shopInfoDTO) {
        return shopInfoService.updateShopInfo(shopInfoDTO);
    }

    @PostMapping("/shopInfoStatistic")
    @ApiOperation("我的店铺信息采购订单统计")
    public Result shopInfoStatistic(@RequestParam("shopId") Long shopId) {
//        return shopInfoService.shopInfoStatistic(shopId);
        return Result.ok();
    }

    @PostMapping("/deleteShop")
    @ApiOperation("删除店铺")
    public Result deleteShop(@RequestParam("shopId") Long shopId) {
        return shopInfoService.deleteShop(shopId);
    }

    @PostMapping("/addShop")
    @ApiOperation("新增店铺")
    public Result addShop(@RequestBody ShopInfoDTO shopInfoDTO) {
        return shopInfoService.addShop(shopInfoDTO);
    }

}
