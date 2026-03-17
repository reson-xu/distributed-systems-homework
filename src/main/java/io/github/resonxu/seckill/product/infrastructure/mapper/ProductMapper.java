package io.github.resonxu.seckill.product.infrastructure.mapper;

import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper {

    ProductDetail selectDetailById(@Param("productId") Long productId);
}
