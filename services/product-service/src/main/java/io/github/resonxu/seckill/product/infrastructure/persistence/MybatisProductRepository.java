package io.github.resonxu.seckill.product.infrastructure.persistence;

import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.domain.repository.ProductRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 基于 MyBatis 的商品仓储实现。
 */
@Mapper
public interface MybatisProductRepository extends ProductRepository {

    @Override
    ProductDetail findDetailById(@Param("productId") Long productId);
}
