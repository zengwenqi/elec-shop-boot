package elec.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.balance.dto.ServiceTicketCreateDTO;
import elec.shop.pojo.balance.dto.ServiceTicketQueryDTO;
import elec.shop.pojo.balance.vo.ServiceTicketVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.balance.ServiceTicketService;
import elec.shop.service.purchase.PurchaserInfoService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Api(tags = "工单管理")
@RestController
@RequestMapping("/service/ticket")
@RequiredArgsConstructor
@Slf4j
public class ServiceTicketController {

    private final ServiceTicketService serviceTicketService;
    private final SysUserService sysUserService;
    private final PurchaserInfoService purchaserInfoService;

    @ApiOperation("创建工单")
    @PostMapping("/create")
//    @PreAuthorize("hasPermission(null, 'purchaser', 'merchant', 'admin', 'superadmin')")
    public Result<?> createTicket(@RequestBody @Validated ServiceTicketCreateDTO dto) {
        try {
            Long userId = AllContextUtils.getLoginSysUser().getUserId();
            Long ticketId = serviceTicketService.createTicket(dto, userId);
            return Result.ok(ticketId);
        } catch (Exception e) {
            log.error("创建工单失败", e);
            return Result.fail("创建工单失败：" + e.getMessage());
        }
    }

    @ApiOperation("查询工单列表")
    @PostMapping("/list")
    @DataSource(DataSourceType.SLAVE)
//    @PreAuthorize("hasPermission(null, 'purchaser', 'merchant', 'admin', 'superadmin')")
    public Result<?> queryTickets(@RequestBody ServiceTicketQueryDTO dto) {
        try {
            Page<ServiceTicketVO> page = serviceTicketService.queryTickets(dto);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("查询工单列表失败", e);
            return Result.fail("查询工单列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取工单详情")
    @GetMapping("/detail/{ticketId}")
    @DataSource(DataSourceType.SLAVE)
//    @PreAuthorize("hasPermission(null, 'purchaser', 'merchant', 'admin', 'superadmin')")
    public Result<?> getTicketDetail(@PathVariable Long ticketId) {
        try {
            Long userId = AllContextUtils.getLoginSysUser().getUserId();
            ServiceTicketVO vo = serviceTicketService.getTicketDetail(ticketId, userId);
            return Result.ok(vo);
        } catch (Exception e) {
            log.error("获取工单详情失败", e);
            return Result.fail("获取工单详情失败：" + e.getMessage());
        }
    }

    @ApiOperation("关闭工单")
    @PutMapping("/close/{ticketId}")
//    @PreAuthorize("hasPermission(null, 'purchaser', 'merchant', 'admin', 'superadmin')")
    public Result<?> closeTicket(@PathVariable Long ticketId) {
        try {
            Long userId = AllContextUtils.getLoginSysUser().getUserId();
            Boolean result = serviceTicketService.closeTicket(ticketId, userId);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("关闭工单失败", e);
            return Result.fail("关闭工单失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户列表（管理员用）")
    @GetMapping("/users")
    @DataSource(DataSourceType.SLAVE)
//    @PreAuthorize("hasPermission(null, 'admin', 'superadmin')")
    public Result<?> getUserList() {
        try {
            // 获取所有用户列表，供管理员选择
            return Result.ok(sysUserService.getAllUserList());
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.fail("获取用户列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取采购员列表（管理员用）")
    @GetMapping("/purchasers")
    @DataSource(DataSourceType.SLAVE)
//    @PreAuthorize("hasPermission(null, 'admin', 'superadmin')")
    public Result<?> getPurchaserList() {
        try {
            // 获取采购员信息列表，供管理员选择
            return Result.ok(purchaserInfoService.selectPurchaserInfoList());
        } catch (Exception e) {
            log.error("获取采购员列表失败", e);
            return Result.fail("获取采购员列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("导出工单数据")
    @PostMapping("/export")
    @DataSource(DataSourceType.SLAVE)
//    @PreAuthorize("hasPermission(null, 'admin', 'superadmin')")
    public void exportTickets(@RequestBody ServiceTicketQueryDTO dto, HttpServletResponse response) {
        try {
            // 调用服务层导出方法
            serviceTicketService.exportTickets(dto, response);
        } catch (Exception e) {
            log.error("导出工单数据失败", e);
            // 重置响应
            response.reset();
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }
}
