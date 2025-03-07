package com.ldgen.ldgenpricutrebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.ldgen.ldgenpricutrebackend.mapper")
public class ldgenPricutreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ldgenPricutreBackendApplication.class, args);
    }

}
