package com.ldgen.ldgenpricutrebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@MapperScan("com.ldgen.ldgenpricutrebackend.mapper")

public class ldgenPricutreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ldgenPricutreBackendApplication.class, args);
    }

}
