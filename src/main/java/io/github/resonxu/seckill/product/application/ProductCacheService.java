package io.github.resonxu.seckill.product.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.redis.DistributedLockClient;
import io.github.resonxu.seckill.common.redis.RedisCacheClient;
import io.github.resonxu.seckill.common.redis.RedisCacheValue;
import io.github.resonxu.seckill.common.redis.RedisKeyConstants;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailVO;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 基于缓存的商品查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final ProductService productService;
    private final RedisCacheClient redisCacheClient;
    private final DistributedLockClient distributedLockClient;

    /**
     * Cache Aside 查询商品详情。
     * 查询命中缓存直接返回；缓存未命中时回源数据库并回填缓存；
     * 对不存在的数据写入短 TTL 空值，降低缓存穿透风险；
     * 利用 Redisson 互斥锁限制热点 Key 的缓存重建并为正常数据添加随机 TTL，降低击穿和雪崩风险。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    public ProductDetailVO getProductDetail(Long productId) {
        String cacheKey = RedisKeyConstants.buildProductDetailKey(productId);

        ProductDetailVO cachedResponse = readCachedProductDetail(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        return rebuildProductDetailCache(productId, cacheKey);
    }


    private ProductDetailVO rebuildProductDetailCache(Long productId, String cacheKey) {
        String lockKey = RedisKeyConstants.buildProductDetailLockKey(productId);
        long deadlineNanos = System.nanoTime() + RedisKeyConstants.PRODUCT_DETAIL_CACHE_REBUILD_MAX_WAIT.toNanos();

        // 热点 Key 只允许一个请求回源，其余请求在总等待窗口内轮询缓存结果。
        while (true) {
            if (tryAcquireLock(lockKey)) {
                try {
                    ProductDetailVO cachedResponse = readCachedProductDetail(cacheKey);
                    if (cachedResponse != null) {
                        return cachedResponse;
                    }
                    return loadFromDatabaseAndCache(productId, cacheKey);
                } finally {
                    distributedLockClient.unlock(lockKey);
                }
            }

            ProductDetailVO cachedResponse = readCachedProductDetail(cacheKey);
            if (cachedResponse != null) {
                return cachedResponse;
            }

            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                break;
            }
            sleepBeforeRetry(remainingNanos);
        }

        throw new BusinessException(ResultCode.SYSTEM_ERROR, "product detail cache rebuild timeout");
    }

    private ProductDetailVO readCachedProductDetail(String cacheKey) {
        RedisCacheValue<ProductDetailVO> cacheValue = redisCacheClient.get(
                cacheKey,
                ProductDetailVO.class,
                RedisKeyConstants.EMPTY_CACHE_VALUE
        );

        if (cacheValue.isMiss()) {
            return null;
        }
        if (RedisCacheValue.emptyMarker()) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return cacheValue.value();
    }

    private ProductDetailVO loadFromDatabaseAndCache(Long productId, String cacheKey) {
        ProductDetail productDetail = productService.getDetailById(productId);

        if (productDetail == null) {
            redisCacheClient.setString(
                    cacheKey,
                    RedisKeyConstants.EMPTY_CACHE_VALUE,
                    RedisKeyConstants.PRODUCT_DETAIL_EMPTY_TTL
            );
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        ProductDetailVO response = ProductDetailVO.builder()
                .productId(productDetail.getId())
                .productName(productDetail.getProductName())
                .price(productDetail.getPrice())
                .status(productDetail.getStatus())
                .availableStock(productDetail.getAvailableStock())
                .build();

        long cacheTtlMillis = buildProductDetailTtlMillis();
        redisCacheClient.set(cacheKey, response, Duration.ofMillis(cacheTtlMillis));
        return response;
    }

    private boolean tryAcquireLock(String lockKey) {
        return distributedLockClient.tryLock(
                lockKey,
                Duration.ZERO,
                RedisKeyConstants.PRODUCT_DETAIL_LOCK_LEASE_TIME
        );
    }

    private void sleepBeforeRetry(long remainingNanos) {
        long pollIntervalMillis = RedisKeyConstants.PRODUCT_DETAIL_CACHE_REBUILD_POLL_INTERVAL.toMillis();
        long remainingMillis = TimeUnit.NANOSECONDS.toMillis(remainingNanos);
        long sleepMillis = Math.min(pollIntervalMillis, Math.max(1L, remainingMillis));

        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "interrupted while waiting for product detail cache");
        }
    }

    private long buildProductDetailTtlMillis() {
        Duration jitter = RedisKeyConstants.PRODUCT_DETAIL_TTL_JITTER;
        long randomMillis = jitter.isZero() ? 0L : ThreadLocalRandom.current().nextLong(jitter.toMillis() + 1);
        return RedisKeyConstants.PRODUCT_DETAIL_TTL.toMillis() + randomMillis;
    }
}
