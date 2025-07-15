package elec.shop.aspect;

import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.config.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 自动数据源切换处理
 */
@Aspect
@Order(1)
@Component
public class DataSourceAutoAspect {

    // 定义读方法的前缀
    private static final List<String> QUERY_PREFIX = Arrays.asList(
            "get", "query", "find", "list", "select", "count", "export"
    );

    @Pointcut("execution(* elec.shop.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 先检查是否有手动指定的数据源
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DataSource dataSource = method.getAnnotation(DataSource.class);

        if (dataSource != null) {
            // 如果有手动指定的数据源注解，使用手动指定的
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        } else {
            // 如果没有手动指定，则根据方法名判断
            String methodName = method.getName().toLowerCase();
            boolean isQueryMethod = QUERY_PREFIX.stream().anyMatch(prefix -> methodName.startsWith(prefix.toLowerCase()));

            // 设置数据源
            if (isQueryMethod) {
                DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE.name());
            } else {
                DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.MASTER.name());
            }
        }

        try {
            return point.proceed();
        } finally {
            // 清理数据源
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }
}
