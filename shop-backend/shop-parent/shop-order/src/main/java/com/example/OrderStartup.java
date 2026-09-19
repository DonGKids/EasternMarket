package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * shop-order 订单微服务启动类
 */
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.example.mapper")
public class OrderStartup {
    public static void main(String[] args) {
        SpringApplication.run(OrderStartup.class, args);
    }
}
