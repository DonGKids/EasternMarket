package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * shop-coupon 优惠券微服务启动类
 */
@SpringBootApplication
@MapperScan("com.example.mapper")
public class CouponStartup {
    public static void main(String[] args) {
        SpringApplication.run(CouponStartup.class, args);
    }
}
