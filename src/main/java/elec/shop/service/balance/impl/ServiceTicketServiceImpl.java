package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.balance.TicketReplyMapper;
import elec.shop.mapper.purchase.PurchaserInfoMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.balance.ServiceTicket;
import elec.shop.pojo.balance.TicketReply;
import elec.shop.pojo.balance.dto.ServiceTicketCreateDTO;
import elec.shop.pojo.balance.dto.ServiceTicketQueryDTO;
import elec.shop.pojo.balance.vo.ServiceTicketExportVO;
import elec.shop.pojo.balance.vo.ServiceTicketVO;
import elec.shop.pojo.purchase.PurchaserInfo;
import elec.shop.pojo.purchase.vo.PurchaserOrderExportVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.balance.ServiceTicketService;
import elec.shop.mapper.balance.ServiceTicketMapper;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.DateUtil;
import elec.shop.utils.ExcelUtils;
import elec.shop.utils.MinioUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.OutputStream;
import java.io.IOException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
* @author Lenovo
* @description 针对表【service_ticket(工单表)】的数据库操作Service实现
* @createDate 2025-06-23 16:45:35
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceTicketServiceImpl extends ServiceImpl<ServiceTicketMapper, ServiceTicket>
    implements ServiceTicketService{

    private final TicketReplyMapper ticketReplyMapper;
    private final PurchaserInfoMapper purchaserInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    // 工单编号计数器
    private static final AtomicInteger TICKET_COUNTER = new AtomicInteger(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final SysUserService sysUserService;
    private final MinioUtil minioUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTicket(ServiceTicketCreateDTO dto, Long userId) {
        try {
            ServiceTicket ticket = new ServiceTicket();

            // 生成工单编号
            String ticketNo = generateTicketNo();
            ticket.setTicketNo(ticketNo);
            ticket.setUserId(userId);

            // 根据用户类型设置采购员ID
            SysUser currentUser = sysUserMapper.selectById(AllContextUtils.getLoginSysUser().getUserId());
            if (currentUser.getUserType() == 3) {
                // 如果是采购员，查找对应的采购员ID
                PurchaserInfo purchaserInfo = purchaserInfoMapper.selectOne(
                    new LambdaQueryWrapper<PurchaserInfo>()
                        .eq(PurchaserInfo::getUserId, userId)
                );
                if (purchaserInfo != null) {
                    ticket.setPurchaseId(purchaserInfo.getPurchaserId());
                }
            }

            ticket.setTicketType(convertTypeToInt(dto.getType()));
            ticket.setPriority(dto.getPriority());
            ticket.setTitle(dto.getTitle());
            ticket.setContent(dto.getContent());
            ticket.setOrderId(dto.getOrderId());
            ticket.setStatus(0); // 待处理

            // 处理图片列表
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                String imagesJson = objectMapper.writeValueAsString(dto.getImages());
                ticket.setImages(imagesJson);
            }

            ticket.setCreatedAt(new Date());
            ticket.setUpdatedAt(new Date());
            ticket.setIsDeleted(0);

            // 保存工单
            this.save(ticket);

            log.info("用户{}创建工单成功，工单编号：{}", userId, ticketNo);
            return ticket.getTicketId();

        } catch (Exception e) {
            log.error("创建工单失败", e);
            throw new BusinessException("创建工单失败：" + e.getMessage());
        }
    }

    @Override
    public Page<ServiceTicketVO> queryTickets(ServiceTicketQueryDTO dto) {
        Page<ServiceTicket> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<ServiceTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTicket::getIsDeleted, 0);

        Integer userType = sysUserMapper.selectById(AllContextUtils.getLoginSysUser().getUserId()).getUserType();

        if (userType==3){
            wrapper.eq(ServiceTicket::getPurchaseId, purchaserInfoMapper.selectOne(
                    new LambdaQueryWrapper<PurchaserInfo>()
                            .eq(PurchaserInfo::getUserId, AllContextUtils.getLoginSysUser().getUserId())
            ).getPurchaserId());
        }
        if (userType==4){
            wrapper.eq(ServiceTicket::getUserId, AllContextUtils.getLoginSysUser().getUserId());
        }

        if (dto.getQueryType().equals("PURCHASER")){
            wrapper.isNotNull(ServiceTicket::getPurchaseId);
        }

        if (dto.getQueryType().equals("USER")){
            wrapper.isNull(ServiceTicket::getPurchaseId);
        }
        // 用户ID过滤
        if (dto.getUserId() != null) {
            wrapper.eq(ServiceTicket::getUserId, dto.getUserId());
        }

        // 采购员ID过滤
        if (dto.getPurchaseId() != null) {
            wrapper.eq(ServiceTicket::getPurchaseId, dto.getPurchaseId());
        }

        // 类型过滤
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(ServiceTicket::getTicketType, convertTypeToInt(dto.getType()));
        }

        // 状态过滤
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(ServiceTicket::getStatus, convertStatusToInt(dto.getStatus()));
        }

        // 关键字搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(ServiceTicket::getTitle, dto.getKeyword())
                    .or().like(ServiceTicket::getContent, dto.getKeyword())
                    .or().like(ServiceTicket::getTicketNo, dto.getKeyword()));
        }

        // 优先级过滤
        if (dto.getPriority() != null && dto.getPriority() != -1) {
            wrapper.eq(ServiceTicket::getPriority, dto.getPriority());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(ServiceTicket::getCreatedAt);

        Page<ServiceTicket> ticketPage = this.page(page, wrapper);

        // 转换为VO
        Page<ServiceTicketVO> voPage = new Page<>();
        voPage.setCurrent(ticketPage.getCurrent());
        voPage.setSize(ticketPage.getSize());
        voPage.setTotal(ticketPage.getTotal());
        voPage.setPages(ticketPage.getPages());

        List<ServiceTicketVO> voList = new ArrayList<>();
        for (ServiceTicket ticket : ticketPage.getRecords()) {
            ServiceTicketVO vo = convertToVO(ticket);
            vo.setImages(new ArrayList<>(minioUtil.getObjectUrls(vo.getImages()).values()));
            voList.add(vo);
        }
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ServiceTicketVO getTicketDetail(Long ticketId, Long userId) {
        // 获取当前用户信息
        SysUser currentUser = AllContextUtils.getLoginSysUser();
        SysUser byId = sysUserService.getById(currentUser.getUserId());
        Integer userType = byId.getUserType();

        LambdaQueryWrapper<ServiceTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTicket::getTicketId, ticketId)
                .eq(ServiceTicket::getIsDeleted, 0);

        // 如果不是管理员，只能查看自己的工单
        if (userType!=1 && userType!=2) {
            wrapper.eq(ServiceTicket::getUserId, userId);
        }

        ServiceTicket ticket = this.getOne(wrapper);
        if (ticket == null) {
            throw new BusinessException("工单不存在或无权限访问");
        }

        ServiceTicketVO vo = convertToVO(ticket);

        // 获取最新的客服回复
        LambdaQueryWrapper<TicketReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(TicketReply::getTicketId, ticketId)
                .eq(TicketReply::getReplyType, 2) // 客服回复
                .orderByDesc(TicketReply::getCreatedAt)
                .last("LIMIT 1");

        TicketReply latestReply = ticketReplyMapper.selectOne(replyWrapper);
        if (latestReply != null) {
            vo.setReply(latestReply.getReplyContent());
            vo.setReplyTime(DateUtil.formatDateTime(latestReply.getCreatedAt()));
        }
        Map<String, String> objectUrls = minioUtil.getObjectUrls(vo.getImages());
        vo.setImages(new ArrayList<>(objectUrls.values()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean closeTicket(Long ticketId, Long userId) {
        // 获取当前用户信息
        SysUser currentUser = AllContextUtils.getLoginSysUser();
        SysUser byId = sysUserService.getById(currentUser.getUserId());
        Integer userType = byId.getUserType();

        LambdaQueryWrapper<ServiceTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTicket::getTicketId, ticketId)
                .eq(ServiceTicket::getIsDeleted, 0);

        // 如果不是管理员，只能关闭自己的工单
        if (userType!=1 && userType!=2) {
            wrapper.eq(ServiceTicket::getUserId, userId);
        }

        ServiceTicket ticket = this.getOne(wrapper);
        if (ticket == null) {
            throw new BusinessException("工单不存在或无权限访问");
        }

        if (ticket.getStatus() == 3) {
            throw new BusinessException("工单已关闭");
        }

        ticket.setStatus(3); // 已关闭
        ticket.setCloseTime(new Date());
        ticket.setUpdatedAt(new Date());

        boolean result = this.updateById(ticket);

        if (result) {
            log.info("用户{}关闭工单成功，工单ID：{}", userId, ticketId);
        }

        return result;
    }

    /**
     * 生成工单编号
     */
    private String generateTicketNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int sequence = TICKET_COUNTER.getAndIncrement();
        if (sequence >= 9999) {
            TICKET_COUNTER.set(0);
        }
        return String.format("FB%s%04d", timestamp, sequence);
    }

    /**
     * 转换类型字符串为整数
     */
    private Integer convertTypeToInt(String type) {
        if (!StringUtils.hasText(type)) {
            return 0;
        }
        switch (type) {
            case "TECHNICAL_SUPPORT": return 1;
            case "FEATURE_REQUEST": return 2;
            case "BUG_REPORT": return 3;
            case "ACCOUNT_ISSUE": return 4;
            default: return 0;
        }
    }

    /**
     * 转换状态字符串为整数
     */
    private Integer convertStatusToInt(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        switch (status) {
            case "待处理": return 0;
            case "处理中": return 1;
            case "已解决": return 2;
            case "已关闭": return 3;
            default: return null;
        }
    }

    /**
     * 转换整数类型为字符串
     */
    private String convertIntToType(Integer type) {
        if (type == null) {
            return "其他问题";
        }
        switch (type) {
            case 1: return "技术支持";
            case 2: return "功能建议";
            case 3: return "问题反馈";
            case 4: return "账户问题";
            default: return "其他问题";
        }
    }

    /**
     * 转换整数状态为字符串
     */
    private String convertIntToStatus(Integer status) {
        if (status == null) {
            return "待处理";
        }
        switch (status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "已解决";
            case 3: return "已关闭";
            default: return "待处理";
        }
    }

    /**
     * 转换实体为VO
     */
    private ServiceTicketVO convertToVO(ServiceTicket ticket) {
        ServiceTicketVO vo = new ServiceTicketVO();
        vo.setTicketId(ticket.getTicketId());
        vo.setTicketNo(ticket.getTicketNo());
        vo.setUserId(ticket.getUserId());

        // 查询用户信息
        if (ticket.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(ticket.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setMobile(user.getMobile());
            }
        }

        // 查询采购员信息
        vo.setPurchaseId(ticket.getPurchaseId());
        if (ticket.getPurchaseId() != null) {
            PurchaserInfo purchaserInfo = purchaserInfoMapper.selectById(ticket.getPurchaseId());
            if (purchaserInfo != null) {
                vo.setPurchaserCode(purchaserInfo.getPurchaserCode());
            }
        }

        vo.setType(convertIntToType(ticket.getTicketType()));
        vo.setPriority(ticket.getPriority());
        vo.setTitle(ticket.getTitle());
        vo.setContent(ticket.getContent());
        vo.setStatus(convertIntToStatus(ticket.getStatus()));

        // 处理图片列表
        if (StringUtils.hasText(ticket.getImages())) {
            try {
                List<String> images = objectMapper.readValue(ticket.getImages(), new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (Exception e) {
                log.warn("解析工单图片失败：{}", e.getMessage());
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        // 格式化时间
        if (ticket.getCreatedAt() != null) {
            vo.setCreateTime(DateUtil.formatDateTime(ticket.getCreatedAt()));
        }
        if (ticket.getCloseTime() != null) {
            vo.setCloseTime(DateUtil.formatDateTime(ticket.getCloseTime()));
        }

        return vo;
    }

    @Override
    public void exportTickets(ServiceTicketQueryDTO dto, HttpServletResponse response) throws IOException {
        // 获取当前用户信息
        Long currentUserId = AllContextUtils.getLoginSysUser().getUserId();
        Integer userType = sysUserMapper.selectById(currentUserId).getUserType();

        // 获取采购员ID（如果是采购员用户）
        Long purchaserId = null;
        if (userType == 3) {
            PurchaserInfo purchaserInfo = purchaserInfoMapper.selectOne(
                    new LambdaQueryWrapper<PurchaserInfo>()
                            .eq(PurchaserInfo::getUserId, currentUserId)
            );
            if (purchaserInfo != null) {
                purchaserId = purchaserInfo.getPurchaserId();
            }
        }

        // 准备查询参数
        Integer typeInt = StringUtils.hasText(dto.getType()) ? convertTypeToInt(dto.getType()) : null;
        Integer statusInt = StringUtils.hasText(dto.getStatus()) ? convertStatusToInt(dto.getStatus()) : null;

        // 使用多表查询获取导出数据
        List<ServiceTicketExportVO> exportData = this.baseMapper.selectTicketsForExport(
                userType,
                userType == 4 ? currentUserId : null,  // 商户用户只能看自己的工单
                purchaserId,  // 采购员用户只能看自己负责的工单
                dto.getQueryType(),
                dto.getType(),
                typeInt,
                dto.getStatus(),
                statusInt,
                dto.getKeyword(),
                dto.getPriority(),
                dto.getUserId(),  // 管理员可以按用户ID过滤
                dto.getPurchaseId()  // 管理员可以按采购员ID过滤
        );

        // 处理图片数据
        for (ServiceTicketExportVO exportVO : exportData) {
            if (StringUtils.hasText(exportVO.getImagesStr())) {
                try {
                    List<String> imageKeys = objectMapper.readValue(exportVO.getImagesStr(), new TypeReference<List<String>>() {});
                    if (!imageKeys.isEmpty()) {
                        Map<String, String> imageUrls = minioUtil.getObjectUrls(imageKeys);
                        // 将多个图片URL用分号分隔
                        String imagesStr = String.join("; ", imageUrls.values());
                        exportVO.setImagesStr(imagesStr);
                    } else {
                        exportVO.setImagesStr("");
                    }
                } catch (Exception e) {
                    log.warn("解析工单图片失败：{}", e.getMessage());
                    exportVO.setImagesStr("");
                }
            } else {
                exportVO.setImagesStr("");
            }

            // 清理所有字符串字段
            exportVO.setTicketNo(exportVO.getTicketNo());
            exportVO.setTitle(exportVO.getTitle());
            exportVO.setContent(exportVO.getContent());
            exportVO.setUsername(exportVO.getUsername());
            exportVO.setMobile(exportVO.getMobile());
            exportVO.setPurchaserCode(exportVO.getPurchaserCode());
        }

        log.info("开始导出工单数据，共{}条记录", exportData.size());

        // 使用ExcelUtils工具类导出（不使用合并功能，避免数据问题）
        String fileName = "工单数据_" + System.currentTimeMillis();
        ExcelUtils.exportExcel(response, exportData, fileName, "工单数据", ServiceTicketExportVO.class);

        log.info("工单数据导出完成");
    }

    /**
     * 获取优先级文本
     */
    private String getPriorityText(Integer priority) {
        if (priority == null) {
            return "普通";
        }
        switch (priority) {
            case 0: return "低";
            case 1: return "普通";
            case 2: return "高";
            case 3: return "紧急";
            default: return "普通";
        }
    }
}




