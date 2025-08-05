package elec.shop.aspect;

import com.alibaba.fastjson.JSON;
import elec.shop.annotation.OperationLog;
import elec.shop.config.DataSourceType;
import elec.shop.config.DynamicDataSourceContextHolder;
import elec.shop.pojo.purchase.PurchaseOrderLog;
import elec.shop.pojo.sys.SysLoginLog;
import elec.shop.pojo.sys.SysOperationLog;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.PurchaseOrderLogService;
import elec.shop.service.sys.SysLoginLogService;
import elec.shop.service.sys.SysOperationLogService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.IpUtils;
import elec.shop.utils.Result;
import elec.shop.utils.SnowflakeLogIdGenerator;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysLoginLogService loginLogService;
    private final SysOperationLogService operationLogService;
    private final PurchaseOrderLogService purchaseOrderLogService;

    @Pointcut("@annotation(elec.shop.annotation.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 保存原有数据源
        String originalDataSource = DynamicDataSourceContextHolder.getDataSourceType();
        // 强制切换到主库
        DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.MASTER.name());

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
            // 恢复原有数据源
            if (originalDataSource != null) {
                DynamicDataSourceContextHolder.setDataSourceType(originalDataSource);
            } else {
                DynamicDataSourceContextHolder.clearDataSourceType();
            }
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
                saveLoginLog(ip, userAgent, loginSysUser, time, location, ex, result);
            } else if (operationLog.isPurchaseOrder()) {
                // 处理采购订单日志
                savePurchaseOrderLog(loginSysUser,operationLog,time,result);
            } else {
                // 处理操作日志
                saveOperationLog(joinPoint,signature,ip,location,loginSysUser,operationLog,time,request,userAgent,ex);
            }
        } catch (Exception e) {
            log.error("日志记录异常：", e);
        }
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(String ip, UserAgent userAgent, SysUser loginSysUser, long time, String location, Exception ex, Object result){
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setLogId(new SnowflakeLogIdGenerator(0L, 0L).generateId());
        loginLog.setIp(ip);
        loginLog.setUserId(loginSysUser.getUserId());
        loginLog.setUsername(loginSysUser.getUsername());
        loginLog.setTime(time);
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
    }

    /**
     * 保存采购订单日志
     */
    private void savePurchaseOrderLog(SysUser loginSysUser, OperationLog operationLog, long time, Object result){
        PurchaseOrderLog orderLog = new PurchaseOrderLog();
        orderLog.setLogId(new SnowflakeLogIdGenerator(1L, 1L).generateId());
        orderLog.setOperatorId(loginSysUser.getUserId());
        orderLog.setOperatorName(loginSysUser.getUsername());
        orderLog.setTime(time);
        orderLog.setOperationType(operationLog.operationType());
        orderLog.setOperationDesc(operationLog.description());
        orderLog.setCreatedAt(new Date());

        // 从返回结果中获取订单ID和状态
        if (result instanceof Result) {
            Result<?> res = (Result<?>) result;
            if (res.getData() != null) {
                if (res.getData() instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) res.getData();
                    if (data.containsKey("orderId")) {
                        orderLog.setOrderId((Long) data.get("orderId"));
                    }
                    if (data.containsKey("orderStatus")) {
                        orderLog.setOrderStatus((Integer) data.get("orderStatus"));
                    }
                }
            }
        }

        // 设置状态
        orderLog.setTenantId(0L);

        purchaseOrderLogService.save(orderLog);
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(ProceedingJoinPoint joinPoint,MethodSignature signature,String ip, String location,SysUser loginSysUser, OperationLog operationLog, long time, HttpServletRequest request, UserAgent userAgent, Exception ex){
        SysOperationLog sysLog = new SysOperationLog();
        sysLog.setLogId(new SnowflakeLogIdGenerator(0L, 0L).generateId()); // 实际项目中建议使用ID生成器
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
}
