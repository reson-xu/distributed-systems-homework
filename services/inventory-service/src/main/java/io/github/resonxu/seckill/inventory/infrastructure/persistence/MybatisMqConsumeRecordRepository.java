package io.github.resonxu.seckill.inventory.infrastructure.persistence;

import io.github.resonxu.seckill.inventory.domain.model.MqConsumeRecord;
import io.github.resonxu.seckill.inventory.domain.repository.MqConsumeRecordRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 基于 MyBatis 的 MQ 消费记录仓储实现。
 */
@Mapper
public interface MybatisMqConsumeRecordRepository extends MqConsumeRecordRepository {

    @Override
    MqConsumeRecord findByBizTypeAndMessageKey(
            @Param("bizType") String bizType,
            @Param("messageKey") String messageKey
    );

    @Override
    int insert(MqConsumeRecord mqConsumeRecord);

    @Override
    int updateConsumeStatus(
            @Param("bizType") String bizType,
            @Param("messageKey") String messageKey,
            @Param("consumeStatus") Integer consumeStatus
    );
}
