package io.github.resonxu.seckill.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 支付服务启动入口。
 */
@SpringBootApplication
@EnableFeignClients
public class PaymentServiceApplication {

    /**
     * 启动支付服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
