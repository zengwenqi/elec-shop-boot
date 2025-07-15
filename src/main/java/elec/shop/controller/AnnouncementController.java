package elec.shop.controller;

import elec.shop.annotation.DataSource;
import elec.shop.annotation.OperationLog;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.announcement.MessageRecord;
import elec.shop.pojo.announcement.dto.MessageRequest;
import elec.shop.pojo.announcement.enums.MessageTypeEnum;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.announcement.MessageRecordService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Api(tags = "公告管理")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRecordService messageRecordService;

    @ApiOperation("处理实时公告消息")
    @MessageMapping("/notice/send")
    @SendTo("/topic/notice")
    public MessageRecord processNotice(MessageRequest request) {
        MessageRecord messageRecord = convertToMessageRecord(request);
        messageRecord.setMessageType(MessageTypeEnum.SYSTEM_NOTICE.getCode());
        return messageRecord;
    }

    @ApiOperation("处理私信消息")
    @MessageMapping("/notice/private/{userId}")
    public void sendPrivateMessage(@DestinationVariable String userId, MessageRequest request) {
        MessageRecord messageRecord = convertToMessageRecord(request);
        messageRecord.setMessageType(MessageTypeEnum.PRIVATE_MESSAGE.getCode());
        messageRecord.setReceiver(userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/notice", messageRecord);
    }

    @ApiOperation("发布系统公告")
    @PostMapping("/publish")
    @OperationLog(module = "公告管理", operationType = "发布公告")
    public Result<Object> publishNotice(@RequestBody MessageRequest request) {
        try {
            MessageRecord messageRecord = convertToMessageRecord(request);
            messageRecord.setMessageType(MessageTypeEnum.SYSTEM_NOTICE.getCode());

            // 保存公告到数据库
            boolean save = messageRecordService.save(messageRecord);
            if (save) {
                // 广播到所有订阅者
                messagingTemplate.convertAndSend("/topic/notice", messageRecord);
                return Result.ok(true);
            }
            return Result.fail().message("发布公告失败");
        } catch (Exception e) {
            log.error("发布公告异常", e);
            return Result.fail().message("发布公告异常：" + e.getMessage());
        }
    }

    @ApiOperation("发送私信")
    @PostMapping("/private/{userId}")
    @OperationLog(module = "公告管理", operationType = "发送私信")
    public Result<Object> sendPrivateNotice(@PathVariable String userId, @RequestBody MessageRequest request) {
        try {
            MessageRecord messageRecord = convertToMessageRecord(request);
            messageRecord.setMessageType(MessageTypeEnum.PRIVATE_MESSAGE.getCode());
            messageRecord.setReceiver(userId);

            // 保存私信到数据库
            boolean save = messageRecordService.save(messageRecord);
            if (save) {
                // 发送到特定用户
                messagingTemplate.convertAndSendToUser(userId, "/queue/notice", messageRecord);
                return Result.ok(true);
            }
            return Result.fail().message("发送私信失败");
        } catch (Exception e) {
            log.error("发送私信异常", e);
            return Result.fail().message("发送私信异常：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户消息列表")
    @GetMapping("/messages")
    @DataSource(DataSourceType.SLAVE)
    public Result<Object> getUserMessages(
            @RequestParam(required = false) Integer messageType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            SysUser loginUser = AllContextUtils.getLoginSysUser();
            // 获取用户的所有消息：系统公告和私信
            List<MessageRecord> messages = messageRecordService.getUserMessages(
                    loginUser.getUsername(), messageType, startDate, endDate);

            // 按发送时间倒序排序
            messages.sort((a, b) -> b.getSendTime().compareTo(a.getSendTime()));
            return Result.ok(messages);
        } catch (Exception e) {
            log.error("获取消息列表异常", e);
            return Result.fail().message("获取消息列表异常：" + e.getMessage());
        }
    }

    @ApiOperation("标记消息已读")
    @PutMapping("/read/{recordId}")
    @DataSource(DataSourceType.MASTER)
    public Result<Object> markMessageAsRead(@PathVariable Long recordId) {
        try {
            SysUser loginUser = AllContextUtils.getLoginSysUser();
            boolean success = messageRecordService.markMessageAsRead(recordId, loginUser.getUsername());
            return Result.ok(success);
        } catch (Exception e) {
            log.error("标记消息已读异常", e);
            return Result.fail().message("标记消息已读异常：" + e.getMessage());
        }
    }

    private MessageRecord convertToMessageRecord(MessageRequest request) {
        SysUser loginUser = AllContextUtils.getLoginSysUser();
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setTitle(request.getTitle());
        messageRecord.setContent(request.getContent());
        messageRecord.setSender(loginUser.getUsername());
        messageRecord.setCreatedAt(new Date());
        messageRecord.setSendTime(new Date());
        messageRecord.setSendStatus(1); // 默认发送成功
        return messageRecord;
    }
}
