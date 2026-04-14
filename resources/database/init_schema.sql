CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username_is_deleted (username, is_deleted),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-上架, 0-下架',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS t_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    locked_stock INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_product_id_is_deleted (product_id, is_deleted),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY COMMENT '业务订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    request_id VARCHAR(64) NOT NULL COMMENT '请求幂等ID',
    order_amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态',
    fail_reason VARCHAR(256) DEFAULT NULL COMMENT '失败原因',
    create_source VARCHAR(32) NOT NULL DEFAULT 'SECKILL' COMMENT '创建来源',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_request_id (request_id),
    UNIQUE KEY uk_user_product_is_deleted (user_id, product_id, is_deleted),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS t_mq_consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    biz_type VARCHAR(64) NOT NULL COMMENT '业务类型',
    message_key VARCHAR(128) NOT NULL COMMENT '消息唯一键',
    consume_status TINYINT NOT NULL DEFAULT 0 COMMENT '消费状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_biz_type_message_key (biz_type, message_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MQ消费记录表';

CREATE TABLE IF NOT EXISTS t_inventory_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    biz_id BIGINT NOT NULL COMMENT '业务ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    flow_type VARCHAR(32) NOT NULL COMMENT '流水类型',
    change_count INT NOT NULL COMMENT '变更数量',
    before_available_stock INT NOT NULL COMMENT '变更前可用库存',
    after_available_stock INT NOT NULL COMMENT '变更后可用库存',
    source_event VARCHAR(64) NOT NULL COMMENT '来源事件',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_biz_id (biz_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

CREATE TABLE IF NOT EXISTS t_payment_order (
    id BIGINT PRIMARY KEY COMMENT '支付单ID',
    order_id BIGINT NOT NULL COMMENT '业务订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    request_id VARCHAR(64) NOT NULL COMMENT '支付请求幂等ID',
    payment_amount DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    payment_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态',
    fail_reason VARCHAR(256) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_payment_request_id (request_id),
    UNIQUE KEY uk_payment_order_id (order_id),
    KEY idx_payment_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付单表';
