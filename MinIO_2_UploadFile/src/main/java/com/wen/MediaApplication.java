package com.wen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// 启动Swagger2相关技术的注解：会扫描当前类所在包，及子包中所有类型的swagger相关注解，做swagger文档的定制
@EnableSwagger2
// @MapperScan注解：扫描 指定 文件夹
@MapperScan("com.wen.mapper")
@SpringBootApplication
public class MediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaApplication.class, args);
    }
}
