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

    /**
     * 分页查询商品列表。
     *
     * @param offset 偏移量
     * @param limit 查询条数
     * @return 商品列表
     */
    java.util.List<ProductDetail> listProducts(int offset, int limit);

    /**
     * 分页查询可秒杀商品列表。
     *
     * @param offset 偏移量
     * @param limit 查询条数
     * @return 可秒杀商品列表
     */
    java.util.List<ProductDetail> listSeckillProducts(int offset, int limit);
}
