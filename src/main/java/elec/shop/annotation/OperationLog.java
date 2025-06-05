package elec.shop.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型（如：登录、查询、新增、修改、删除等）
     */
    String operationType() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;

    /**
     * 是否保存响应数据
     */
    boolean saveResponseData() default true;

    /**
     * 是否是登录日志
     */
    boolean isLogin() default false;
}
