package io.github.resonxu.seckill.inventory.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.inventory.domain.model.InventorySnapshot;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryRepository;
import io.github.resonxu.seckill.inventory.interfaces.vo.InventoryDetailVO;
import io.github.resonxu.seckill.inventory.interfaces.vo.InventoryStockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 库存查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class InventoryQueryAppService {

    private final InventoryRepository inventoryRepository;

    /**
     * 查询商品当前可用库存。
     *
     * @param productId 商品ID
     * @return 可用库存信息
     */
    public InventoryStockVO getAvailableStock(Long productId) {
        Integer availableStock = inventoryRepository.findAvailableStockByProductId(productId);
        if (availableStock == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "inventory not found");
        }
        return InventoryStockVO.builder()
                .productId(productId)
                .availableStock(availableStock)
                .build();
    }

    /**
     * 查询库存详情。
     *
     * @param productId 商品ID
     * @return 库存详情
     */
    public InventoryDetailVO getInventoryDetail(Long productId) {
        InventorySnapshot inventorySnapshot = inventoryRepository.findSnapshotByProductId(productId);
        if (inventorySnapshot == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "inventory not found");
        }
        return InventoryDetailVO.builder()
                .productId(inventorySnapshot.getProductId())
                .totalStock(inventorySnapshot.getTotalStock())
                .availableStock(inventorySnapshot.getAvailableStock())
                .lockedStock(inventorySnapshot.getLockedStock())
                .build();
    }
}
