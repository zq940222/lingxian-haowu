package com.lingxian.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户端API启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.lingxian.user", "com.lingxian.common"})
@MapperScan(basePackages = {"com.lingxian.common.mapper", "com.lingxian.user.mapper"})
public class UserApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApiApplication.class, args);
    }

}
