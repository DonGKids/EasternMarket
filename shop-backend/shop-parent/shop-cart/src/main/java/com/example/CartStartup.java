package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * shop-cart 购物车微服务启动类
 * 通过 Feign 调用 shop-product 获取商品快照信息
 */
@SpringBootApplication
@MapperScan("com.example.mapper")
@EnableFeignClients(basePackages = "com.example.feign")
public class CartStartup {
    public static void main(String[] args) {
        SpringApplication.run(CartStartup.class, args);
    }
}
