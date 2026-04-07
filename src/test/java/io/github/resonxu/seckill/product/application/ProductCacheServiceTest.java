package io.github.resonxu.seckill.product.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.redis.DistributedLockClient;
import io.github.resonxu.seckill.common.redis.RedisCacheClient;
import io.github.resonxu.seckill.common.redis.RedisCacheValue;
import io.github.resonxu.seckill.common.redis.RedisKeyConstants;
import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailVO;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private RedisCacheClient redisCacheClient;

    @Mock
    private DistributedLockClient distributedLockClient;

    @InjectMocks
    private ProductCacheService productCacheService;

    @Test
    void shouldReturnProductDetailFromCache() throws Exception {
        ProductDetailVO cachedResponse = ProductDetailVO.builder()
                .productId(1L)
                .productName("phone")
                .price(new BigDecimal("100.00"))
                .status(1)
                .availableStock(10)
                .build();
        String cacheKey = RedisKeyConstants.buildProductDetailKey(1L);

        when(redisCacheClient.get(cacheKey, ProductDetailVO.class, RedisKeyConstants.EMPTY_CACHE_VALUE))
                .thenReturn(RedisCacheValue.hit(cachedResponse));

        ProductDetailVO response = productCacheService.getProductDetail(1L);

        assertEquals(1L, response.getProductId());
        assertEquals("phone", response.getProductName());
        verify(productService, never()).getDetailById(1L);
    }

    @Test
    void shouldLoadFromDatabaseAndWriteCacheWhenMiss() throws Exception {
        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(1L);
        productDetail.setProductName("phone");
        productDetail.setPrice(new BigDecimal("100.00"));
        productDetail.setStatus(1);
        productDetail.setAvailableStock(10);
        String cacheKey = RedisKeyConstants.buildProductDetailKey(1L);
        String lockKey = RedisKeyConstants.buildProductDetailLockKey(1L);

        when(redisCacheClient.get(cacheKey, ProductDetailVO.class, RedisKeyConstants.EMPTY_CACHE_VALUE))
                .thenReturn(RedisCacheValue.miss(), RedisCacheValue.miss());
        when(distributedLockClient.tryLock(lockKey, Duration.ZERO, RedisKeyConstants.PRODUCT_DETAIL_LOCK_LEASE_TIME))
                .thenReturn(true);
        when(productService.getDetailById(1L)).thenReturn(productDetail);

        ProductDetailVO response = productCacheService.getProductDetail(1L);

        assertEquals(1L, response.getProductId());
        assertEquals(10, response.getAvailableStock());
        ArgumentCaptor<Object> cacheValueCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(redisCacheClient).set(eq(cacheKey), cacheValueCaptor.capture(), ttlCaptor.capture());
        ProductDetailVO cachedValue = (ProductDetailVO) cacheValueCaptor.getValue();
        assertEquals(1L, cachedValue.getProductId());
        assertTrue(ttlCaptor.getValue().toMillis() >= RedisKeyConstants.PRODUCT_DETAIL_TTL.toMillis());
        assertTrue(ttlCaptor.getValue().toMillis()
                <= RedisKeyConstants.PRODUCT_DETAIL_TTL.toMillis()
                + RedisKeyConstants.PRODUCT_DETAIL_TTL_JITTER.toMillis());
        verify(distributedLockClient).unlock(lockKey);
    }

    @Test
    void shouldWriteEmptyMarkerWhenProductNotFound() {
        String cacheKey = RedisKeyConstants.buildProductDetailKey(99L);
        String lockKey = RedisKeyConstants.buildProductDetailLockKey(99L);

        when(redisCacheClient.get(cacheKey, ProductDetailVO.class, RedisKeyConstants.EMPTY_CACHE_VALUE))
                .thenReturn(RedisCacheValue.miss(), RedisCacheValue.miss());
        when(distributedLockClient.tryLock(lockKey, Duration.ZERO, RedisKeyConstants.PRODUCT_DETAIL_LOCK_LEASE_TIME))
                .thenReturn(true);
        when(productService.getDetailById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productCacheService.getProductDetail(99L));

        assertEquals("2001", exception.getCode());
        verify(redisCacheClient).setString(
                cacheKey,
                RedisKeyConstants.EMPTY_CACHE_VALUE,
                RedisKeyConstants.PRODUCT_DETAIL_EMPTY_TTL
        );
        verify(distributedLockClient).unlock(lockKey);
    }

    @Test
    void shouldRejectWhenEmptyMarkerExists() {
        String cacheKey = RedisKeyConstants.buildProductDetailKey(99L);

        when(redisCacheClient.get(cacheKey, ProductDetailVO.class, RedisKeyConstants.EMPTY_CACHE_VALUE))
                .thenReturn(RedisCacheValue.emptyMarker());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productCacheService.getProductDetail(99L));

        assertEquals("2001", exception.getCode());
        verify(productService, never()).getDetailById(99L);
    }

    @Test
    void shouldReturnCachedValueAfterLockContention() throws Exception {
        ProductDetailVO cachedResponse = ProductDetailVO.builder()
                .productId(1L)
                .productName("phone")
                .price(new BigDecimal("100.00"))
                .status(1)
                .availableStock(10)
                .build();
        String cacheKey = RedisKeyConstants.buildProductDetailKey(1L);
        String lockKey = RedisKeyConstants.buildProductDetailLockKey(1L);

        when(redisCacheClient.get(cacheKey, ProductDetailVO.class, RedisKeyConstants.EMPTY_CACHE_VALUE))
                .thenReturn(RedisCacheValue.miss(), RedisCacheValue.hit(cachedResponse));
        when(distributedLockClient.tryLock(lockKey, Duration.ZERO, RedisKeyConstants.PRODUCT_DETAIL_LOCK_LEASE_TIME))
                .thenReturn(false);

        ProductDetailVO response = productCacheService.getProductDetail(1L);

        assertEquals(1L, response.getProductId());
        verify(productService, never()).getDetailById(1L);
        verify(redisCacheClient, never()).set(anyString(), any(), any());
    }
}
