package io.github.resonxu.seckill.product.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.json.JsonUtil;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.product.config.ProductRedisKeys;
import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.domain.repository.ProductRepository;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailVO;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于缓存的商品查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class ProductDetailQueryAppService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final JsonUtil jsonUtil;

    /**
     * Cache Aside 查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    public ProductDetailVO getProductDetail(Long productId) {
        String cacheKey = ProductRedisKeys.buildProductDetailKey(productId);
        ProductDetailVO cachedResponse = readCachedProductDetail(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        return rebuildProductDetailCache(productId, cacheKey);
    }

    private ProductDetailVO rebuildProductDetailCache(Long productId, String cacheKey) {
        String lockKey = ProductRedisKeys.buildProductDetailLockKey(productId);
        long deadlineNanos = System.nanoTime() + ProductRedisKeys.PRODUCT_DETAIL_CACHE_REBUILD_MAX_WAIT.toNanos();

        while (true) {
            if (tryAcquireLock(lockKey)) {
                try {
                    ProductDetailVO cachedResponse = readCachedProductDetail(cacheKey);
                    if (cachedResponse != null) {
                        return cachedResponse;
                    }
                    return loadFromDatabaseAndCache(productId, cacheKey);
                } finally {
                    unlock(lockKey);
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
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue == null) {
            return null;
        }
        if (ProductRedisKeys.EMPTY_CACHE_VALUE.equals(cachedValue)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        try {
            return jsonUtil.fromJson(cachedValue, ProductDetailVO.class);
        } catch (IllegalStateException exception) {
            stringRedisTemplate.delete(cacheKey);
            return null;
        }
    }

    private ProductDetailVO loadFromDatabaseAndCache(Long productId, String cacheKey) {
        ProductDetail productDetail = productRepository.findDetailById(productId);

        if (productDetail == null) {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    ProductRedisKeys.EMPTY_CACHE_VALUE,
                    ProductRedisKeys.PRODUCT_DETAIL_EMPTY_TTL
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

        stringRedisTemplate.opsForValue().set(
                cacheKey,
                jsonUtil.toJson(response),
                Duration.ofMillis(buildProductDetailTtlMillis())
        );
        return response;
    }

    private boolean tryAcquireLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(
                    Duration.ZERO.toMillis(),
                    ProductRedisKeys.PRODUCT_DETAIL_LOCK_LEASE_TIME.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "interrupted while acquiring product detail lock");
        }
    }

    private void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    private void sleepBeforeRetry(long remainingNanos) {
        long pollIntervalMillis = ProductRedisKeys.PRODUCT_DETAIL_CACHE_REBUILD_POLL_INTERVAL.toMillis();
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
        Duration jitter = ProductRedisKeys.PRODUCT_DETAIL_TTL_JITTER;
        long randomMillis = jitter.isZero() ? 0L : ThreadLocalRandom.current().nextLong(jitter.toMillis() + 1);
        return ProductRedisKeys.PRODUCT_DETAIL_TTL.toMillis() + randomMillis;
    }
}
