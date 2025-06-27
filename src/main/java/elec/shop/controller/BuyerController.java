package elec.shop.controller;

import elec.shop.pojo.purchase.dto.ShopInfoDTO;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "采购员管理")
@RestController
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerController {


    @PostMapping("/updateShopInfo")
    @ApiOperation("查询我的待采购订单")
    public Result<Object> updateShopInfo(@RequestBody ShopInfoDTO shopInfoDTO) {
        return Result.ok();
    }


    @PostMapping("/a")
    @ApiOperation("价格上报")
    public Result<Object> a(@RequestBody ShopInfoDTO shopInfoDTO) {
        return Result.ok();
    }


    @PostMapping("/c")
    @ApiOperation("我的待采购订单详情")
    public Result<Object> b(@RequestBody ShopInfoDTO shopInfoDTO) {
        return Result.ok();
    }


    @PostMapping("/d")
    @ApiOperation("更新我的待采购订单状态")
    public Result<Object> c(@RequestBody ShopInfoDTO shopInfoDTO) {
        return Result.ok();
    }
}
