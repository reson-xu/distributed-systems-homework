package io.github.resonxu.seckill.common.aop;

import io.github.resonxu.seckill.common.annotation.Idempotent;
import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 幂等控制切面。
 */
@Aspect
@Component
public class IdempotentAspect {

    private static final String IDEMPOTENT_KEY_PREFIX = "seckill:request";

    private final StringRedisTemplate stringRedisTemplate;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 创建幂等切面。
     *
     * @param stringRedisTemplate Redis 字符串模板
     */
    public IdempotentAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 对标记幂等注解的方法做请求去重。
     *
     * @param joinPoint 切点
     * @param idempotent 幂等注解
     * @return 业务执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String redisKey = buildRedisKey(joinPoint, idempotent);
        Duration ttl = Duration.ofSeconds(idempotent.ttlSeconds());
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", ttl);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (idempotent.releaseOnException()) {
                stringRedisTemplate.delete(redisKey);
            }
            throw throwable;
        }
    }

    private String buildRedisKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(methodSignature.getMethod());
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        if (parameterNames != null) {
            for (int index = 0; index < parameterNames.length; index++) {
                context.setVariable(parameterNames[index], args[index]);
            }
        }

        String uniqueKey = expressionParser.parseExpression(idempotent.key()).getValue(context, String.class);
        if (uniqueKey == null || uniqueKey.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "idempotent key can not be blank");
        }
        return String.join(":", IDEMPOTENT_KEY_PREFIX, idempotent.scene(), uniqueKey);
    }
}
