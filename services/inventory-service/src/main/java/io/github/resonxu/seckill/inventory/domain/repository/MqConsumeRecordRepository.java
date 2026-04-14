package io.github.resonxu.seckill.inventory.domain.repository;

import io.github.resonxu.seckill.inventory.domain.model.MqConsumeRecord;

/**
 * MQ 消费记录领域仓储。
 */
public interface MqConsumeRecordRepository {

    /**
     * 根据业务类型和消息键查询消费记录。
     *
     * @param bizType 业务类型
     * @param messageKey 消息键
     * @return 消费记录，不存在时返回 null
     */
    MqConsumeRecord findByBizTypeAndMessageKey(String bizType, String messageKey);

    /**
     * 新增消费记录。
     *
     * @param mqConsumeRecord 消费记录
     * @return 影响行数
     */
    int insert(MqConsumeRecord mqConsumeRecord);

    /**
     * 更新消费状态。
     *
     * @param bizType 业务类型
     * @param messageKey 消息键
     * @param consumeStatus 消费状态
     * @return 影响行数
     */
    int updateConsumeStatus(String bizType, String messageKey, Integer consumeStatus);
}
