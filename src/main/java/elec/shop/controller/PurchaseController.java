package elec.shop.controller;

import elec.shop.pojo.purchase.ShopInfo;
import elec.shop.service.purchase.AccountBalanceService;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.service.purchase.ShopInfoService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "采购管理")
@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final ShopInfoService shopInfoService;
    private final AccountBalanceService accountBalanceService;
    private final FinanceAccountService financeAccountService;

    @PostMapping("/shopInfo")
    @ApiOperation("查询我的店铺信息")
    public Result queryShopInfo() {
        List<ShopInfo> shopInfoList = shopInfoService.queryShopInfo();
        return Result.ok(shopInfoList);
    }

}
