package com.wen.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author wen
 * @version 1.0
 * @description TODO 自动配置类，用于配置MinioClient的实例化和初始化
 * @date 2023/4/1 12:45
 */
@Configuration
@ConditionalOnClass(MinioClient.class)// 标识只有在MinioClient类存在时才会执行此自动配置类
@EnableConfigurationProperties(MinioAutoProperties.class)
public class MinioAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MinioAutoConfiguration.class);

    @Resource
    private MinioAutoProperties minioAutoProperties;

    /**
     * 配置MinioClient实例的Bean，Bean名称默认为方法名
     */
    @Bean
    public MinioClient minioClient() {
        log.info("开始初始化MinioClient, url为{}, accessKey为:{}", minioAutoProperties.getEndpoint(), minioAutoProperties.getAccessKey());
        // 使用MinioClient.builder()创建MinioClient实例
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(minioAutoProperties.getEndpoint()) // 设置Minio服务端地址
                .credentials(minioAutoProperties.getAccessKey(), minioAutoProperties.getSecretKey())// 设置访问凭证（accessKey和secretKey）

                .build(); // 构建MinioClient实例
        // 设置超时时间
        minioClient.setTimeout(
                minioAutoProperties.getConnectTimeout(), // 连接超时时间
                minioAutoProperties.getWriteTimeout(), // 写入超时时间
                minioAutoProperties.getReadTimeout() // 读取超时时间
        );

        // 开始检测存储桶是否存在
        if (minioAutoProperties.isCheckBucket()) {
            log.info("checkBucket属性为{}, 开始检测桶是否存在...", minioAutoProperties.isCheckBucket());
            String bucketName = minioAutoProperties.getBucket();
            // 如果存储桶不存在
            if (!checkBucket(bucketName, minioClient)) {
                log.info("文件桶[{}]不存在, 开始检查是否可以新建桶", bucketName);
                // 是否允许新建存储桶
                if (minioAutoProperties.isCreateBucket()) {
                    log.info("createBucket属性为{},开始新建文件桶", minioAutoProperties.isCreateBucket());
                    // 新建存储桶
                    createBucket(bucketName, minioClient);
                }
            }
            log.info("文件桶[{}]已存在, minio客户端连接成功!", bucketName);
        } else {
            log.info("桶不存在, 请检查桶名称是否正确或者将checkBucket属性改为false");
            throw new RuntimeException("桶不存在, 请检查桶名称是否正确或者将checkBucket属性改为false");
        }
        return minioClient;
    }

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName  存储桶名称
     * @param minioClient MinioClient实例
     * @return 返回一个布尔值，代表存储桶是否存在
     */
    private boolean checkBucket(String bucketName, MinioClient minioClient) {
        boolean isExists = false;
        try {
            // 使用MinioClient的bucketExists方法判断存储桶是否存在
            isExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("未能检查存储桶是否存在", e);
        }
        return isExists;
    }

    /**
     * 新建存储桶
     *
     * @param bucketName  存储桶名称
     * @param minioClient MinioClient实例
     */
    private void createBucket(String bucketName, MinioClient minioClient) {
        try {
            // 创建桶
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .objectLock(false)// 不启用对象锁
                            .build()
            );
            // 设置桶的访问策略为允许匿名访问 S3 桶中的所有对象
            String policyJson = "{\n" +
                    "    \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Action\": [ \"s3:GetObject\", \"s3:PutObject\", \"s3:DeleteObject\", \"s3:ListBucket\" ],\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": { \"AWS\": [ \"*\" ] },\n" +
                    "            \"Resource\": [ \"arn:aws:s3:::" + bucketName + "/*\", \"arn:aws:s3:::" + bucketName + "\" ],\n" +
                    "            \"Sid\": \"PublicReadWrite\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"Version\": \"2012-10-17\"\n" +
                    "}";
            // 设置桶的访问策略
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policyJson)// 设置访问策略JSON
                            .build()
            );
            log.info("文件桶[{}]新建成功, minio客户端已连接", bucketName);
        } catch (Exception e) {
            log.info("创建默认桶失败：{}", e.getMessage());
            throw new RuntimeException("创建默认桶失败", e);
        }
    }
}
