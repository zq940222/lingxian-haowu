package com.lingxian.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商户端API启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.lingxian.merchant", "com.lingxian.common"})
public class MerchantApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantApiApplication.class, args);
    }

}
