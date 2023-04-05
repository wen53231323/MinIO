package com.wen.config;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author wen
 * @version 1.0
 * @description TODO 自动配置类，将配置文件中与Minio对象存储服务相关的属性值读取到MinioAutoProperties类对应的字段中
 * @date 2023/4/1 12:45
 */
@Data
@Validated // 开启校验
@Component // 表示这个类自动被 Spring 扫描并注入到容器中
@ConfigurationProperties(prefix = "minio") // 将配置文件中 minio 前缀的属性值绑定到该类对应的字段中
public class MinioAutoProperties {

    /**
     * minio服务地址，不可为空且必须符合URL格式
     */
    @NotEmpty(message = "minio服务地址不可为空")
    @URL(message = "minio服务地址格式错误")
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * minio认证账户，不可为空
     */
    @NotEmpty(message = "minio认证账户不可为空")
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * minio认证密码，不可为空
     */
    @NotEmpty(message = "minio认证密码不可为空")
    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * 全局默认文件桶，可选参数，优先级最低
     */
    @Value("${minio.extend.defaultBucket:default-bucket}")
    private String bucket;

    /**
     * 当桶不存在时是否自动创建桶，默认为true
     */
    @Value("${minio.extend.createBucket:true}")
    private boolean createBucket;

    /**
     * 启动时是否检查桶是否存在，默认为true
     */
    @Value("${minio.extend.checkBucket:true}")
    private boolean checkBucket;

    /**
     * HTTP连接超时时间，以毫秒为单位，默认为0，表示没有超时时间
     */
    @Value("${minio.extend.connectTimeout:0}")
    private long connectTimeout;

    /**
     * HTTP写入超时时间，以毫秒为单位，默认为0，表示没有超时时间
     */
    @Value("${minio.extend.writeTimeout:0}")
    private long writeTimeout;

    /**
     * HTTP读取超时时间，以毫秒为单位，默认为0，表示没有超时时间
     */
    @Value("${minio.extend.readTimeout:0}")
    private long readTimeout;

}
