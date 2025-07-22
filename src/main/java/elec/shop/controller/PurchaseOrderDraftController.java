package elec.shop.controller;

import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.purchase.PurchaseOrderDraft;
import elec.shop.service.purchase.PurchaseOrderDraftService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/purchase/draft")
@Api(tags = "采购订单暂存接口")
@Slf4j
@RequiredArgsConstructor
public class PurchaseOrderDraftController {

    private final PurchaseOrderDraftService draftService;

    @PostMapping("/save")
    @ApiOperation("保存暂存数据")
    @DataSource(DataSourceType.MASTER)
    public Result<?> saveDraft(@RequestBody PurchaseOrderDraft draft) {
        try {
            // 设置当前用户ID
            draft.setUserId(AllContextUtils.getLoginSysUser().getUserId());
            boolean success = draftService.saveDraft(draft);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("保存采购订单暂存数据失败", e);
            return Result.fail().message("保存失败");
        }
    }

    @GetMapping("/get")
    @ApiOperation("获取暂存数据")
    @DataSource(DataSourceType.SLAVE)
    public Result<?> getDraft() {
        try {
            Long userId = AllContextUtils.getLoginSysUser().getUserId();
            PurchaseOrderDraft draft = draftService.getDraftByUserId(userId);
            return Result.ok(draft);
        } catch (Exception e) {
            log.error("获取采购订单暂存数据失败", e);
            return Result.fail().message("获取失败");
        }
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除暂存数据")
    @DataSource(DataSourceType.MASTER)
    public Result<?> deleteDraft() {
        try {
            Long userId = AllContextUtils.getLoginSysUser().getUserId();
            boolean success = draftService.deleteDraftByUserId(userId);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("删除采购订单暂存数据失败", e);
            return Result.fail().message("删除失败");
        }
    }
}
