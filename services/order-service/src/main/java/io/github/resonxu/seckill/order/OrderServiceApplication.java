package io.github.resonxu.seckill.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动入口。
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    /**
     * 启动订单服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
