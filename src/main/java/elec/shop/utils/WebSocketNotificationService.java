package elec.shop.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket通知服务
 * 用于发送WebSocket消息通知
 */
@Slf4j
@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 发送任务状态变更通知
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param newStatus 新状态
     */
    public void sendTaskStatusChangeNotification(Long userId, Long taskId, Integer newStatus) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "TASK_STATUS_CHANGE");
            message.put("userId", userId);
            message.put("taskId", taskId);
            message.put("newStatus", newStatus);
            message.put("timestamp", System.currentTimeMillis());
            
            // 发送到指定用户的私有队列
            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/notice", 
                message
            );
            
            log.info("任务状态变更通知发送成功 - 用户ID: {}, 任务ID: {}, 新状态: {}", userId, taskId, newStatus);
        } catch (Exception e) {
            log.error("发送任务状态变更通知失败 - 用户ID: {}, 任务ID: {}, 新状态: {}", userId, taskId, newStatus, e);
            throw e;
        }
    }

    /**
     * 发送系统通知
     * @param message 消息内容
     */
    public void sendSystemNotification(Map<String, Object> message) {
        try {
            messagingTemplate.convertAndSend("/topic/notice", message);
            log.info("系统通知发送成功: {}", message);
        } catch (Exception e) {
            log.error("发送系统通知失败: {}", message, e);
            throw e;
        }
    }

    /**
     * 发送私人消息
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendPrivateMessage(Long userId, Map<String, Object> message) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/notice", 
                message
            );
            log.info("私人消息发送成功 - 用户ID: {}, 消息: {}", userId, message);
        } catch (Exception e) {
            log.error("发送私人消息失败 - 用户ID: {}, 消息: {}", userId, message, e);
            throw e;
        }
    }
}