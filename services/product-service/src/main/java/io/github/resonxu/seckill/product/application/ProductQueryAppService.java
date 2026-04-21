package io.github.resonxu.seckill.product.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.product.domain.model.ProductDetail;
import io.github.resonxu.seckill.product.domain.repository.ProductRepository;
import io.github.resonxu.seckill.product.interfaces.vo.ProductAvailabilityVO;
import io.github.resonxu.seckill.product.interfaces.vo.ProductListItemVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商品列表与可售状态查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class ProductQueryAppService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int PRODUCT_STATUS_ON_SHELF = 1;

    private final ProductRepository productRepository;

    /**
     * 查询商品列表。
     *
     * @param page 页码
     * @param size 页大小
     * @return 商品列表
     */
    public List<ProductListItemVO> listProducts(Integer page, Integer size) {
        return productRepository.listProducts(buildOffset(page, size), normalizeSize(size))
                .stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * 查询可秒杀商品列表。
     *
     * @param page 页码
     * @param size 页大小
     * @return 可秒杀商品列表
     */
    public List<ProductListItemVO> listSeckillProducts(Integer page, Integer size) {
        return productRepository.listSeckillProducts(buildOffset(page, size), normalizeSize(size))
                .stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * 查询商品可售状态。
     *
     * @param productId 商品ID
     * @return 可售状态
     */
    public ProductAvailabilityVO getAvailability(Long productId) {
        ProductDetail productDetail = productRepository.findDetailById(productId);
        if (productDetail == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        boolean available = Integer.valueOf(PRODUCT_STATUS_ON_SHELF).equals(productDetail.getStatus())
                && productDetail.getAvailableStock() != null
                && productDetail.getAvailableStock() > 0;
        return ProductAvailabilityVO.builder()
                .productId(productDetail.getId())
                .status(productDetail.getStatus())
                .availableStock(productDetail.getAvailableStock())
                .available(available)
                .build();
    }

    private ProductListItemVO toListItem(ProductDetail productDetail) {
        return ProductListItemVO.builder()
                .productId(productDetail.getId())
                .productName(productDetail.getProductName())
                .price(productDetail.getPrice())
                .status(productDetail.getStatus())
                .availableStock(productDetail.getAvailableStock())
                .build();
    }

    private int buildOffset(Integer page, Integer size) {
        int normalizedPage = page == null || page < 1 ? DEFAULT_PAGE : page;
        return (normalizedPage - 1) * normalizeSize(size);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
