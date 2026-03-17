package io.github.resonxu.seckill.product.application;

import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    public ProductDetail getDetailById(Long productId) {
        return productMapper.selectDetailById(productId);
    }
}
