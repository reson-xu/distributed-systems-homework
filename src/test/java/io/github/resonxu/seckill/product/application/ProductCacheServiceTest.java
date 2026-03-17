package io.github.resonxu.seckill.product.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.redis.RedisKeyConstants;
import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ProductCacheService productCacheService;

    @Test
    void shouldReturnProductDetailFromCache() {
        ProductDetailResponse cachedResponse = ProductDetailResponse.builder()
                .productId(1L)
                .productName("phone")
                .price(new BigDecimal("100.00"))
                .status(1)
                .availableStock(10)
                .build();
        String cacheKey = RedisKeyConstants.buildProductDetailKey(1L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(cachedResponse);

        ProductDetailResponse response = productCacheService.getProductDetail(1L);

        assertEquals(1L, response.getProductId());
        assertEquals("phone", response.getProductName());
        verify(productService, never()).getDetailById(1L);
    }

    @Test
    void shouldLoadFromDatabaseAndWriteCacheWhenMiss() {
        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(1L);
        productDetail.setProductName("phone");
        productDetail.setPrice(new BigDecimal("100.00"));
        productDetail.setStatus(1);
        productDetail.setAvailableStock(10);
        String cacheKey = RedisKeyConstants.buildProductDetailKey(1L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(productService.getDetailById(1L)).thenReturn(productDetail);

        ProductDetailResponse response = productCacheService.getProductDetail(1L);

        assertEquals(1L, response.getProductId());
        assertEquals(10, response.getAvailableStock());
        verify(valueOperations).set(cacheKey, response, RedisKeyConstants.PRODUCT_DETAIL_TTL);
    }

    @Test
    void shouldWriteEmptyMarkerWhenProductNotFound() {
        String cacheKey = RedisKeyConstants.buildProductDetailKey(99L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(productService.getDetailById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productCacheService.getProductDetail(99L));

        assertEquals("2001", exception.getCode());
        verify(valueOperations).set(cacheKey,
                RedisKeyConstants.EMPTY_CACHE_VALUE,
                RedisKeyConstants.PRODUCT_DETAIL_EMPTY_TTL);
    }

    @Test
    void shouldRejectWhenEmptyMarkerExists() {
        String cacheKey = RedisKeyConstants.buildProductDetailKey(99L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(RedisKeyConstants.EMPTY_CACHE_VALUE);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productCacheService.getProductDetail(99L));

        assertEquals("2001", exception.getCode());
        verify(productService, never()).getDetailById(99L);
    }
}
