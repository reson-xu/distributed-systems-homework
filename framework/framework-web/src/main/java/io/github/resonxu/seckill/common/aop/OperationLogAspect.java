package io.github.resonxu.seckill.common.aop;

import io.github.resonxu.seckill.common.annotation.OperationLog;
import io.github.resonxu.seckill.common.json.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 接口操作日志切面。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final JsonUtil jsonUtil;

    /**
     * 记录接口操作日志。
     *
     * @param joinPoint 被拦截的连接点
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(io.github.resonxu.seckill.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        OperationLog operationLog = methodSignature.getMethod().getAnnotation(OperationLog.class);

        HttpServletRequest request = getCurrentRequest();
        String requestMethod = request == null ? "N/A" : request.getMethod();
        String requestUri = request == null ? "N/A" : request.getRequestURI();
        String requestArgs = toJson(joinPoint.getArgs());

        log.info("接口请求开始，描述={}，请求方式={}，请求路径={}，请求参数={}",
                operationLog.description(), requestMethod, requestUri, requestArgs);

        try {
            Object result = joinPoint.proceed();
            log.info("接口请求成功，描述={}，耗时={}ms，响应结果={}",
                    operationLog.description(), System.currentTimeMillis() - startTime, toJson(result));
            return result;
        } catch (Throwable throwable) {
            log.error("接口请求失败，描述={}，耗时={}ms，异常信息={}",
                    operationLog.description(), System.currentTimeMillis() - startTime, throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    private String toJson(Object target) {
        if (target == null) {
            return "null";
        }

        try {
            return jsonUtil.toJson(target);
        } catch (IllegalStateException exception) {
            return Arrays.toString(target instanceof Object[] array ? array : new Object[]{target});
        }
    }
}
