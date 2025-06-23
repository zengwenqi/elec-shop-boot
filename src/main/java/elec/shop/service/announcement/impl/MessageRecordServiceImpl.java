package elec.shop.service.announcement.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.announcement.MessageRecord;
import elec.shop.service.announcement.MessageRecordService;
import elec.shop.mapper.announcement.MessageRecordMapper;
import elec.shop.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author Lenovo
* @description 针对表【message_record(消息记录表)】的数据库操作Service实现
* @createDate 2025-06-19 14:19:39
*/
@Slf4j
@Service
public class MessageRecordServiceImpl extends ServiceImpl<MessageRecordMapper, MessageRecord>
    implements MessageRecordService{

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String MESSAGE_READ_KEY = "message:read:";
    private static final long EXPIRE_DAYS = 30;

    @Override
    public List<MessageRecord> getUserMessages(String username, Integer messageType, String startDate, String endDate) {
        try {
            LambdaQueryWrapper<MessageRecord> queryWrapper = new LambdaQueryWrapper<>();

            // 接收人是当前用户或者是系统公告
//            queryWrapper.and(wrapper -> wrapper
//                .eq(MessageRecord::getReceiver, username)
//                .or()
//                .eq(MessageRecord::getMessageType, 4)
//            );

            // 消息类型过滤
            if (messageType != null) {
                queryWrapper.eq(MessageRecord::getMessageType, messageType);
            }

            // 时间范围过滤
            if (StringUtils.isNotBlank(startDate)) {
                Date start = DateUtil.parseDate(startDate);
                queryWrapper.ge(MessageRecord::getCreatedAt, start);
            }
            if (StringUtils.isNotBlank(endDate)) {
                Date end = DateUtil.parseDate(endDate);
                queryWrapper.le(MessageRecord::getCreatedAt, end);
            }

            // 按创建时间倒序排序
            queryWrapper.orderByDesc(MessageRecord::getCreatedAt);

            List<MessageRecord> messages = this.list(queryWrapper);

            // 设置消息的已读状态
            for (MessageRecord message : messages) {
                boolean isRead = isMessageRead(message.getRecordId(), username);
                message.setSendStatus(isRead ? 1 : 0);
            }

            return messages;
        } catch (Exception e) {
            log.error("获取用户消息列表异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取用户消息列表失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageAsRead(Long recordId, String username) {
        try {
            // 验证消息是否存在且属于当前用户
            LambdaQueryWrapper<MessageRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MessageRecord::getRecordId, recordId)
                .and(wrapper -> wrapper
                    .eq(MessageRecord::getReceiver, username)
                    .or()
                    .eq(MessageRecord::getMessageType, 4) // 系统公告也可以标记已读
                );

            MessageRecord message = this.getOne(queryWrapper);
            if (message == null) {
                log.warn("消息不存在或无权限访问: recordId={}, username={}", recordId, username);
                return false;
            }

            // 在Redis中标记消息为已读
            String key = MESSAGE_READ_KEY + username;
            redisTemplate.opsForSet().add(key, recordId.toString());
            redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);

            return true;
        } catch (Exception e) {
            log.error("标记消息已读异常: {}", e.getMessage(), e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    public boolean isMessageRead(Long recordId, String username) {
        try {
            String key = MESSAGE_READ_KEY + username;
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, recordId.toString()));
        } catch (Exception e) {
            log.error("检查消息已读状态异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean markMessagesAsRead(List<Long> recordIds, String username) {
        try {
            String key = MESSAGE_READ_KEY + username;
            String[] ids = recordIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
            redisTemplate.opsForSet().add(key, ids);
            redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);
            return true;
        } catch (Exception e) {
            log.error("批量标记消息已读异常: {}", e.getMessage(), e);
            return false;
        }
    }
}




