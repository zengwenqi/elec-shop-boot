package elec.shop.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 邮件工具类
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String,Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String userName;// 用户发送者

    // 创建一个发送邮箱验证的方法
    public String sendEmail(String receiver){

        try{
            String subjectName = "登录验证";
            String contentTemplate = "您正在执行登录操作，验证码是%s，2分钟内有效";
            String verifyCode = RandomStringUtils.random(6, "0123456789");
            String content = String.format(contentTemplate, verifyCode);

            redisTemplate.opsForValue().set(receiver,verifyCode, 60, TimeUnit.SECONDS);
            //定义email信息格式
            SimpleMailMessage message = new SimpleMailMessage();
            //设置发件人
            message.setFrom(userName);
            //接收者邮箱，为调用本方法传入的接收者的邮箱xxx@qq.com
            message.setTo(receiver);
            //邮件主题
            message.setSubject(subjectName);
            message.setText(content);
            mailSender.send(message);
            return "success";
        }catch (Exception e){
            log.error("sendEmail failed, exception: ",e);
            return "failed";
        }
    }

    // 自定义发送邮箱和内容的方法
    public String sendCustomEmail(String receiver, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(userName);
            message.setTo(receiver);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Custom email sent to: {}", receiver);
            return "success";
        } catch (Exception e) {
            log.error("sendCustomEmail failed, exception: ", e);
            return "failed";
        }
    }
}
