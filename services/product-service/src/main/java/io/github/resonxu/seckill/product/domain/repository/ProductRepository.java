package io.github.resonxu.seckill.product.domain.repository;

import io.github.resonxu.seckill.product.domain.model.ProductDetail;

/**
 * 商品领域仓储。
 */
public interface ProductRepository {

    /**
     * 根据商品ID查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情，不存在时返回 null
     */
    ProductDetail findDetailById(Long productId);
}
