package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.balance.RechargeFeedback;
import elec.shop.pojo.balance.vo.RechargeFeedbackVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.balance.RechargeFeedbackService;
import elec.shop.mapper.balance.RechargeFeedbackMapper;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【recharge_feedback(充值反馈表)】的数据库操作Service实现
* @createDate 2025-06-29 15:31:03
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class RechargeFeedbackServiceImpl extends ServiceImpl<RechargeFeedbackMapper, RechargeFeedback>
    implements RechargeFeedbackService {

    private final RechargeFeedbackMapper rechargeFeedbackMapper;
    private final MinioUtil minioUtil;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<RechargeFeedbackVO> listByUserId(Long userId) {
        List<RechargeFeedback> list = rechargeFeedbackMapper.selectList(
                new LambdaQueryWrapper<RechargeFeedback>()
                        .eq(RechargeFeedback::getUserId, userId)
                        .eq(RechargeFeedback::getIsDeleted, 0)
        );
        return convertToVOList(list);
    }

    @Override
    public List<RechargeFeedbackVO> listByStatus(String status) {
        List<RechargeFeedback> list = rechargeFeedbackMapper.selectList(
                new LambdaQueryWrapper<RechargeFeedback>()
                        .eq(RechargeFeedback::getStatus, status)
                        .eq(RechargeFeedback::getIsDeleted, 0)
        );
        return convertToVOList(list);
    }

    @Override
    public IPage<RechargeFeedbackVO> selectPage(Integer pageNum, Integer pageSize ,String status, String rechargeNo) {
        try {
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();
            SysUser sysUser = sysUserMapper.selectById(loginSysUser.getUserId());

            // 创建分页对象
            Page<RechargeFeedback> page = new Page<>(pageNum, pageSize);

            // 构建查询条件
            LambdaQueryWrapper<RechargeFeedback> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RechargeFeedback::getIsDeleted, 0)
                    .orderByDesc(RechargeFeedback::getCreatedAt);

            // 如果不是管理员，只能查看自己的记录
            if (sysUser.getUserType() != 1) {
                wrapper.eq(RechargeFeedback::getUserId, sysUser.getUserId());
            }

            if (rechargeNo != null && !rechargeNo.isEmpty()){
                wrapper.like(RechargeFeedback::getRechargeNo, rechargeNo);
            }

            if (status!= null && !status.isEmpty()){
                wrapper.eq(RechargeFeedback::getStatus, status);
            }

            // 执行分页查询
            IPage<RechargeFeedback> entityPage = rechargeFeedbackMapper.selectPage(page, wrapper);

            if (entityPage.getRecords().isEmpty()) {
                return entityPage.convert(this::convertToVO);
            }

            return entityPage.convert(this::convertToVO);
        } catch (Exception e) {
            log.error("Error in selectPage: ", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, String status, Long handlerId, String handler) {
        try {
            RechargeFeedback entity = rechargeFeedbackMapper.selectById(id);
            if (entity == null) {
                log.warn("No feedback found with ID: {}", id);
                return false;
            }
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();
            entity.setStatus(status);
            entity.setHandlerId(loginSysUser.getUserId());
            entity.setHandler(loginSysUser.getUsername());
            entity.setHandleTime(new Date());

            int result = rechargeFeedbackMapper.updateById(entity);
            return result > 0;
        } catch (Exception e) {
            log.error("Error updating status for feedback ID {}: ", id, e);
            throw e;
        }
    }

    @Override
    public List<RechargeFeedbackVO> getExportData(String startTime, String endTime, String status) {
        try {
            // 获取当前登录用户
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();
            SysUser sysUser = sysUserMapper.selectById(loginSysUser.getUserId());

            // 构建查询条件
            LambdaQueryWrapper<RechargeFeedback> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RechargeFeedback::getIsDeleted, 0)
                    .orderByDesc(RechargeFeedback::getCreatedAt);

            // 如果不是管理员，只能导出自己的记录
            if (sysUser.getUserType() != 1) {
                wrapper.eq(RechargeFeedback::getUserId, sysUser.getUserId());
            }

            // 添加时间范围条件
            if (startTime != null && !startTime.isEmpty()) {
                wrapper.ge(RechargeFeedback::getCreatedAt, startTime + " 00:00:00");
            }
            if (endTime != null && !endTime.isEmpty()) {
                wrapper.le(RechargeFeedback::getCreatedAt, endTime + " 23:59:59");
            }

            // 添加状态条件
            if (status != null && !status.isEmpty()) {
                wrapper.eq(RechargeFeedback::getStatus, status);
            }

            // 查询数据
            List<RechargeFeedback> list = rechargeFeedbackMapper.selectList(wrapper);

            // 转换为VO对象
            return convertToVOList(list);
        } catch (Exception e) {
            log.error("获取导出数据失败", e);
            throw new RuntimeException("获取导出数据失败: " + e.getMessage());
        }
    }

    /**
     * 将实体对象转换为VO对象，并处理图片URL
     */
    private RechargeFeedbackVO convertToVO(RechargeFeedback entity) {
        try {
            if (entity == null) {
                return null;
            }

            RechargeFeedbackVO vo = new RechargeFeedbackVO();
            BeanUtils.copyProperties(entity, vo);

            // 处理图片路径列表
            if (vo.getImages() != null && !vo.getImages().isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (String imagePath : vo.getImages()) {
                    try {
                        String previewUrl = minioUtil.getPreviewUrl(imagePath);
                        imageUrls.add(previewUrl);
                    } catch (Exception e) {
                        // 如果获取预览URL失败，保留原始路径
                        imageUrls.add(imagePath);
                    }
                }
                vo.setImages(imageUrls);
            } else {
                log.debug("No images found for feedback ID: {}", entity.getId());
            }

            return vo;
        } catch (Exception e) {
            log.error("Error converting entity to VO: ", e);
            throw e;
        }
    }

    /**
     * 批量转换实体对象为VO对象
     */
    private List<RechargeFeedbackVO> convertToVOList(List<RechargeFeedback> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            log.warn("Empty or null entity list provided for conversion");
            return new ArrayList<>();
        }

        return entityList.stream()
                .map(this::convertToVO)
                .toList();
    }
}




