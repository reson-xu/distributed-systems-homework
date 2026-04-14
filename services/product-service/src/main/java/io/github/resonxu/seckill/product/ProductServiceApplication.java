package io.github.resonxu.seckill.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品服务启动入口。
 */
@SpringBootApplication
public class ProductServiceApplication {

    /**
     * 启动商品服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
