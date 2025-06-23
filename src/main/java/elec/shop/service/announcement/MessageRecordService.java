package elec.shop.service.announcement;

import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.announcement.MessageRecord;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【message_record(消息记录表)】的数据库操作Service
* @createDate 2025-06-19 14:19:39
*/
public interface MessageRecordService extends IService<MessageRecord> {
    
    /**
     * 获取用户消息列表
     *
     * @param username    用户名
     * @param messageType 消息类型
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 消息列表
     */
    List<MessageRecord> getUserMessages(String username, Integer messageType, String startDate, String endDate);

    /**
     * 标记消息为已读
     *
     * @param recordId 消息ID
     * @param username 用户名
     * @return 是否成功
     */
    boolean markMessageAsRead(Long recordId, String username);

    /**
     * 检查消息是否已读
     *
     * @param recordId 消息ID
     * @param username 用户名
     * @return 是否已读
     */
    boolean isMessageRead(Long recordId, String username);

    /**
     * 批量标记消息为已读
     *
     * @param recordIds 消息ID列表
     * @param username 用户名
     * @return 是否成功
     */
    boolean markMessagesAsRead(List<Long> recordIds, String username);
}
