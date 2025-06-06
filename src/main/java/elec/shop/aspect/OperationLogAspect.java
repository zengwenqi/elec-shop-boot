package elec.shop.aspect;

import com.alibaba.fastjson.JSON;
import elec.shop.annotation.OperationLog;
import elec.shop.pojo.sys.SysLoginLog;
import elec.shop.pojo.sys.SysOperationLog;
import elec.shop.pojo.sys.SysUser;
import elec.shop.security.CustomUserDetails;
import elec.shop.service.sys.SysLoginLogService;
import elec.shop.service.sys.SysOperationLogService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.IpUtils;
import elec.shop.utils.Result;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private SysLoginLogService loginLogService;

    @Resource
    private SysOperationLogService operationLogService;

    @Pointcut("@annotation(elec.shop.annotation.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 执行时长(毫秒)
            long time = System.currentTimeMillis() - beginTime;
            // 保存日志
            saveLog(point, time, result, exception);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time, Object result, Exception ex) {
        try {
            // 获取当前请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog operationLog = method.getAnnotation(OperationLog.class);

            // 获取用户代理信息
            String userAgentString = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

            // 获取IP地址
            String ip = IpUtils.getClientIp(request);
            String location = IpUtils.getLocationByIP(ip);

            // 获取当前登录用户信息
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();

            if (operationLog.isLogin()) {
                // 处理登录日志
                SysLoginLog loginLog = new SysLoginLog();
                loginLog.setLogId(System.currentTimeMillis()); // 实际项目中建议使用ID生成器
                loginLog.setIp(ip);
                loginLog.setLocation(location);
                loginLog.setBrowser(userAgent.getBrowser().getName());
                loginLog.setOs(userAgent.getOperatingSystem().getName());
                loginLog.setStatus(ex == null ? 1 : 0);
                loginLog.setMsg(ex == null ? "登录成功" : ex.getMessage());
                loginLog.setLoginTime(new Date());

                // 从登录成功的结果中获取用户信息
                if (result instanceof Result) {
                    Result<?> res = (Result<?>) result;
                    if (res.getData() instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) res.getData();
                        loginLog.setUserId((Long) data.get("userId"));
                        loginLog.setUsername((String) data.get("username"));
                    }
                }

                loginLogService.save(loginLog);
            } else if (operationLog.isPurchaseOrder()) {

            }else {
                // 处理操作日志
                SysOperationLog sysLog = new SysOperationLog();
                sysLog.setLogId(System.currentTimeMillis()); // 实际项目中建议使用ID生成器
                sysLog.setUserId(loginSysUser.getUserId());
                sysLog.setUsername(loginSysUser.getUsername());
                sysLog.setOperationType(operationLog.operationType());
                sysLog.setMethod(request.getMethod() + " " + request.getRequestURI());
                sysLog.setIp(ip);
                sysLog.setLocation(location);
                sysLog.setBrowser(userAgent.getBrowser().getName());
                sysLog.setOs(userAgent.getOperatingSystem().getName());
                sysLog.setStatus(ex == null ? 1 : 0);
                sysLog.setErrorMsg(ex != null ? ex.getMessage() : null);
                sysLog.setTime(time);
                sysLog.setCreatedAt(new Date());

                // 处理请求参数
                if (operationLog.saveRequestData()) {
                    Object[] args = joinPoint.getArgs();
                    String[] parameterNames = signature.getParameterNames();
                    Map<String, Object> params = new HashMap<>();

                    for (int i = 0; i < args.length; i++) {
                        String paramName = parameterNames[i];
                        Object arg = args[i];

                        if (arg instanceof HttpServletRequest) {
                            continue;
                        }

                        if (arg instanceof MultipartFile) {
                            MultipartFile file = (MultipartFile) arg;
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("fileName", file.getOriginalFilename());
                            fileInfo.put("fileSize", file.getSize());
                            fileInfo.put("contentType", file.getContentType());
                            params.put(paramName, fileInfo);
                        } else {
                            params.put(paramName, arg);
                        }
                    }
                    sysLog.setParams(JSON.toJSONString(params));
                }

                operationLogService.save(sysLog);
            }
        } catch (Exception e) {
            log.error("日志记录异常：", e);
        }
    }
}
