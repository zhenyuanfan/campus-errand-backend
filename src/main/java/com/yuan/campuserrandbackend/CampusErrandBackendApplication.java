package com.yuan.campuserrandbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
@MapperScan("com.yuan.campuserrandbackend.mapper")
@SpringBootApplication
public class CampusErrandBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusErrandBackendApplication.class, args);
    }

}
