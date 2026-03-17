package io.github.resonxu.seckill.product.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.redis.RedisKeyConstants;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache Aside 查询商品详情。
     * 查询命中缓存直接返回；缓存未命中时回源数据库并回填缓存；
     * 对不存在的数据写入短 TTL 空值，降低缓存穿透风险。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    public ProductDetailResponse getProductDetail(Long productId) {
        String cacheKey = RedisKeyConstants.buildProductDetailKey(productId);
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

        if (cachedValue instanceof ProductDetailResponse response) {
            return response;
        }
        if (RedisKeyConstants.EMPTY_CACHE_VALUE.equals(cachedValue)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        ProductDetail productDetail = productService.getDetailById(productId);
        if (productDetail == null) {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    RedisKeyConstants.EMPTY_CACHE_VALUE,
                    RedisKeyConstants.PRODUCT_DETAIL_EMPTY_TTL
            );
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(productDetail.getId())
                .productName(productDetail.getProductName())
                .price(productDetail.getPrice())
                .status(productDetail.getStatus())
                .availableStock(productDetail.getAvailableStock())
                .build();
        redisTemplate.opsForValue().set(cacheKey, response, RedisKeyConstants.PRODUCT_DETAIL_TTL);
        return response;
    }
}
