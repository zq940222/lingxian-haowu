package com.lingxian.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 管理后台API启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.lingxian.admin", "com.lingxian.common"})
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }

}
