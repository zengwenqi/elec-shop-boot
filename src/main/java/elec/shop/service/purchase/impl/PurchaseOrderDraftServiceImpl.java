package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.config.RedisConfig;
import elec.shop.exception.BusinessException;
import elec.shop.pojo.purchase.PurchaseOrderDraft;
import elec.shop.service.purchase.PurchaseOrderDraftService;
import elec.shop.mapper.purchase.PurchaseOrderDraftMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
* @author Lenovo
* @description 针对表【purchase_order_draft(采购订单暂存表)】的数据库操作Service实现
* @createDate 2025-07-22 16:36:27
*/
@Service
@Slf4j
public class PurchaseOrderDraftServiceImpl extends ServiceImpl<PurchaseOrderDraftMapper, PurchaseOrderDraft>
    implements PurchaseOrderDraftService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDraft(PurchaseOrderDraft draft) {
        try {
            // 设置过期时间为7天后
            draft.setExpireTime(DateUtils.addDays(new Date(), 7));

            // 先删除旧的暂存数据
            this.lambdaUpdate()
                .eq(PurchaseOrderDraft::getUserId, draft.getUserId())
                .remove();

            // 保存新的暂存数据
            boolean success = this.save(draft);

            if (success) {
                // 更新缓存
                String cacheKey = RedisConfig.PURCHASE_ORDER_DRAFT_KEY + draft.getUserId();
                redisTemplate.opsForValue().set(
                    cacheKey,
                    draft,
                        RedisConfig.CACHE_EXPIRE_HOURS,
                    TimeUnit.HOURS
                );
            }

            return success;
        } catch (Exception e) {
            log.error("保存采购订单暂存数据失败", e);
            throw new BusinessException("保存暂存数据失败");
        }
    }

    @Override
    public PurchaseOrderDraft getDraftByUserId(Long userId) {
        try {
            // 先从缓存获取
            String cacheKey = RedisConfig.PURCHASE_ORDER_DRAFT_KEY + userId;
            PurchaseOrderDraft draft = (PurchaseOrderDraft) redisTemplate.opsForValue().get(cacheKey);

            if (draft != null) {
                return draft;
            }

            // 缓存未命中，从数据库获取
            draft = this.lambdaQuery()
                .eq(PurchaseOrderDraft::getUserId, userId)
                .one();

            if (draft != null) {
                // 检查是否过期
                if (draft.getExpireTime().before(new Date())) {
                    this.deleteDraftByUserId(userId);
                    return null;
                }

                // 放入缓存
                redisTemplate.opsForValue().set(
                    cacheKey,
                    draft,
                        RedisConfig.CACHE_EXPIRE_HOURS,
                    TimeUnit.HOURS
                );
            }

            return draft;
        } catch (Exception e) {
            log.error("获取采购订单暂存数据失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDraftByUserId(Long userId) {
        try {
            // 删除数据库数据
            boolean success = this.lambdaUpdate()
                .eq(PurchaseOrderDraft::getUserId, userId)
                .remove();

            if (success) {
                // 删除缓存
                String cacheKey = RedisConfig.PURCHASE_ORDER_DRAFT_KEY + userId;
                redisTemplate.delete(cacheKey);
            }

            return success;
        } catch (Exception e) {
            log.error("删除采购订单暂存数据失败", e);
            return false;
        }
    }

    @Override
    @DataSource(DataSourceType.MASTER)
    public void cleanExpiredDrafts() {
        try {
            // 删除过期数据
            this.lambdaUpdate()
                .le(PurchaseOrderDraft::getExpireTime, new Date())
                .remove();
        } catch (Exception e) {
            log.error("清理过期采购订单暂存数据失败", e);
        }
    }
}



