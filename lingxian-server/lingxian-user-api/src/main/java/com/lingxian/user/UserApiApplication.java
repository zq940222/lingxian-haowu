package com.lingxian.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端API启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.lingxian.user", "com.lingxian.common"})
public class UserApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApiApplication.class, args);
    }

}
