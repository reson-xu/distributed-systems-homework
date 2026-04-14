package io.github.resonxu.seckill.order.application;

import io.github.resonxu.seckill.common.annotation.Idempotent;
import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.id.SnowflakeIdGenerator;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.order.domain.enums.OrderStatus;
import io.github.resonxu.seckill.order.infrastructure.client.ProductClient;
import io.github.resonxu.seckill.order.infrastructure.client.dto.ProductDetailDTO;
import io.github.resonxu.seckill.order.infrastructure.mq.SeckillOrderProducer;
import io.github.resonxu.seckill.order.infrastructure.mq.message.SeckillOrderCreateMessage;
import io.github.resonxu.seckill.order.infrastructure.persistence.RedisStockReservationRepository;
import io.github.resonxu.seckill.order.infrastructure.persistence.RedisStockReservationRepository.ReserveResult;
import io.github.resonxu.seckill.order.interfaces.dto.SeckillOrderSubmitDTO;
import io.github.resonxu.seckill.order.interfaces.vo.SeckillOrderSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 秒杀下单应用服务。
 */
@Service
@RequiredArgsConstructor
public class SeckillOrderSubmissionAppService {

    private static final Integer PRODUCT_STATUS_ON_SHELF = 1;

    private final ProductClient productClient;
    private final RedisStockReservationRepository redisStockReservationRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final SeckillOrderProducer seckillOrderProducer;

    /**
     * 提交秒杀订单请求。
     *
     * @param userId 用户ID
     * @param request 秒杀请求
     * @return 受理结果
     */
    @Idempotent(
            scene = "seckill-submit",
            key = "#userId + ':' + #request.requestId",
            ttlSeconds = 10,
            releaseOnException = true
    )
    public SeckillOrderSubmitVO submit(Long userId, SeckillOrderSubmitDTO request) {
        ProductDetailDTO productDetail = productClient.getProductDetail(request.getProductId()).getData();
        if (productDetail == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!PRODUCT_STATUS_ON_SHELF.equals(productDetail.getStatus())) {
            throw new BusinessException(ResultCode.SECKILL_PRODUCT_NOT_AVAILABLE);
        }

        ReserveResult reserveResult = redisStockReservationRepository.reserve(
                request.getProductId(),
                userId,
                productDetail.getAvailableStock() == null ? 0 : productDetail.getAvailableStock()
        );
        handleReserveResult(reserveResult);

        long orderId = snowflakeIdGenerator.nextId();
        try {
            seckillOrderProducer.sendCreateOrderMessage(SeckillOrderCreateMessage.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .productId(request.getProductId())
                    .requestId(request.getRequestId())
                    .orderAmount(productDetail.getPrice())
                    .build());
        } catch (Exception exception) {
            redisStockReservationRepository.rollback(request.getProductId(), userId);
            throw new BusinessException(ResultCode.SECKILL_SUBMIT_FAILED);
        }

        return SeckillOrderSubmitVO.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING_CREATE.getDescription())
                .build();
    }

    private void handleReserveResult(ReserveResult reserveResult) {
        switch (reserveResult) {
            case SUCCESS -> {
                return;
            }
            case OUT_OF_STOCK -> throw new BusinessException(ResultCode.SECKILL_OUT_OF_STOCK);
            case REPEAT_ORDER -> throw new BusinessException(ResultCode.SECKILL_REPEAT_ORDER);
            case STOCK_NOT_INITIALIZED, UNKNOWN -> throw new BusinessException(ResultCode.SECKILL_SUBMIT_FAILED);
        }
    }
}
