package com.wen.桶相关操作;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 0:12
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 创建桶 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 创建桶
     */
    @Test
    public void testCreateBucket() {
        String bucketName = "testbucket";
        minioTemplate.createBucket(bucketName);
        boolean exist = minioTemplate.bucketExists(bucketName);
        if (exist) {
            System.out.println("Bucket桶" + bucketName + "创建成功!");
        } else {
            System.out.println("Bucket桶" + bucketName + "创建失败!");
        }
    }

    /**
     * 创建桶，并指定策略
     */
    @Test
    public void test() {
        String bucketName = "testbucket";
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
        minioTemplate.createBucket(bucketName, policyJson);
        boolean exist = minioTemplate.bucketExists(bucketName);
        if (exist) {
            System.out.println("Bucket桶" + bucketName + "创建成功!");
        } else {
            System.out.println("Bucket桶" + bucketName + "创建失败!");
        }
    }
}
