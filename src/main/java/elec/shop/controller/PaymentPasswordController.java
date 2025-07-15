package elec.shop.controller;

import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.utils.Result;
import elec.shop.utils.RsaDecryptUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import elec.shop.service.sys.SysUserService;

@Api(tags = "支付密码管理")
@RestController
@RequestMapping("/payment-password")
@RequiredArgsConstructor
public class PaymentPasswordController {

    private final SysUserService sysUserService;
    private final RsaDecryptUtil rsaDecryptUtil;

    @ApiOperation("检查是否设置支付密码")
    @GetMapping("/check-exists")
    @DataSource(DataSourceType.SLAVE)
    public Result<Boolean> checkPaymentPasswordExists() {
        boolean exists = sysUserService.checkPaymentPasswordExists();
        return Result.ok(exists);
    }

    @ApiOperation("验证支付密码")
    @PostMapping("/verify")
    @DataSource(DataSourceType.SLAVE)
    public Result<Object> verifyPaymentPassword(@RequestBody String encryptedPassword) {
        try {
            String decryptedPassword = RsaDecryptUtil.decryptPayPassword(encryptedPassword);
            boolean isValid = sysUserService.verifyPaymentPassword(decryptedPassword);
            return Result.ok(isValid);
        } catch (Exception e) {
            return Result.fail().message("支付密码验证失败");
        }
    }

    @ApiOperation("设置支付密码")
    @PostMapping("/set")
    @DataSource(DataSourceType.MASTER)
    public Result<Object> setPaymentPassword(@RequestBody String encryptedPassword) {
        try {
            String decryptedPassword = RsaDecryptUtil.decryptPayPassword(encryptedPassword);
            sysUserService.setPaymentPassword(decryptedPassword);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail().message("设置支付密码失败");
        }
    }
}
