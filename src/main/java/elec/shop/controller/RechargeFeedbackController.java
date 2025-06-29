package elec.shop.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.balance.RechargeFeedback;
import elec.shop.pojo.balance.dto.RechargeFeedbackDTO;
import elec.shop.pojo.balance.vo.RechargeFeedbackVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.balance.RechargeFeedbackService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.MinioUtil;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "充值反馈管理")
@RestController
@RequestMapping("/recharge/feedback")
@RequiredArgsConstructor
@Slf4j
public class RechargeFeedbackController {

    private final RechargeFeedbackService rechargeFeedbackService;
    private final MinioUtil minioUtil;

    @PostMapping
    @ApiOperation("新增充值反馈")
    public Result<Object> save(@RequestBody RechargeFeedbackDTO dto) {
        try {
            SysUser sys = AllContextUtils.getLoginSysUser();
            RechargeFeedback rechargeFeedback = new RechargeFeedback();
            BeanUtils.copyProperties(dto, rechargeFeedback);
            rechargeFeedback.setFeedbackNo(AllContextUtils.generateFeedbackNo(sys.getUserId()));
            rechargeFeedback.setUserId(sys.getUserId());
            rechargeFeedback.setSubmitter(sys.getUsername());
            rechargeFeedback.setStatus("PENDING");
            // 确保images不为null
            if (rechargeFeedback.getImages() == null) {
                rechargeFeedback.setImages(new ArrayList<>());
            }

            return Result.ok(rechargeFeedbackService.save(rechargeFeedback));
        } catch (Exception e) {
            log.error("新增充值反馈失败", e);
            return Result.fail().message("新增充值反馈失败：" + e.getMessage());
        }
    }

    @ApiOperation("删除充值反馈")
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Object> delete(@PathVariable Long id) {
        return Result.ok(rechargeFeedbackService.removeById(id));
    }

    @ApiOperation("批量删除充值反馈（逻辑删除）")
    @DeleteMapping("/batch")
    @Transactional
    public Result<Object> deleteBatch(@RequestBody List<Long> ids) {
        return Result.ok(rechargeFeedbackService.removeByIds(ids));
    }

    @ApiOperation("更新充值反馈")
    @PutMapping
    @Transactional
    public Result<Object> update(@RequestBody RechargeFeedbackDTO dto) {
        RechargeFeedback entity = new RechargeFeedback();
        BeanUtils.copyProperties(dto, entity);
        return Result.ok(rechargeFeedbackService.updateById(entity));
    }

    @ApiOperation("更新反馈状态")
    @PutMapping("/status/{id}")
    public Result<Object> updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam Long handlerId,
                               @RequestParam String handler) {
        return Result.ok(rechargeFeedbackService.updateStatus(id, status, handlerId, handler));
    }

    @ApiOperation("根据ID查询充值反馈")
    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        RechargeFeedback feedback = rechargeFeedbackService.getById(id);
        List<String> images = feedback.getImages();
        if (images != null && !images.isEmpty()) {
            // 流式处理并转换为新集合
            List<String> previewImages = images.stream()
                    .map(minioUtil::getPreviewUrl) // 映射为预览URL
                    .collect(Collectors.toList());
            feedback.setImages(previewImages);
        }
        return Result.ok(feedback);
    }

    @ApiOperation("查询所有充值反馈")
    @GetMapping("/list")
    public Result<List<RechargeFeedbackVO>> list() {
        List<RechargeFeedback> list = rechargeFeedbackService.list();
        List<RechargeFeedbackVO> listVO = list.stream().map(e -> {
            RechargeFeedbackVO vo = new RechargeFeedbackVO();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).toList();
        return Result.ok(listVO);
    }

    @ApiOperation("根据用户ID查询充值反馈列表")
    @GetMapping("/user/{userId}")
    public Result<List<RechargeFeedbackVO>> listByUserId(@PathVariable Long userId) {
        return Result.ok(rechargeFeedbackService.listByUserId(userId));
    }

    @ApiOperation("根据状态查询充值反馈列表")
    @GetMapping("/status/{status}")
    public Result<List<RechargeFeedbackVO>> listByStatus(@PathVariable String status) {
        return Result.ok(rechargeFeedbackService.listByStatus(status));
    }

    @ApiOperation("分页查询充值反馈")
    @GetMapping("/page")
    public Result<IPage<RechargeFeedbackVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<RechargeFeedbackVO> page = rechargeFeedbackService.selectPage(pageNum,pageSize);
        return Result.ok(page);
    }

    @ApiOperation("导出充值反馈数据")
    @GetMapping("/export")
    public void exportFeedback(
            @ApiParam(value = "开始时间", example = "2024-01-01") @RequestParam(required = false) String startTime,
            @ApiParam(value = "结束时间", example = "2024-12-31") @RequestParam(required = false) String endTime,
            @ApiParam(value = "状态") @RequestParam(required = false) String status,
            HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("充值反馈数据_" + System.currentTimeMillis(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取数据
            List<RechargeFeedbackVO> dataList = rechargeFeedbackService.getExportData(startTime, endTime, status);

            // 导出数据
            EasyExcel.write(response.getOutputStream(), RechargeFeedbackVO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自适应列宽
                    .sheet("充值反馈数据")
                    .doWrite(dataList);

        } catch (IOException e) {
            log.error("导出充值反馈数据失败", e);
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            try {
                response.getWriter().println("导出失败：" + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
}
