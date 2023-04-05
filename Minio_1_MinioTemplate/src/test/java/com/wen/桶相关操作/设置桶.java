package com.wen.桶相关操作;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import io.minio.messages.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author wen
 * @version 1.0
 * @description TODO  MinioTemplate桶操作测试
 * @date 2023/4/1 18:17
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 设置桶 {

    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 设置公共读写访问策略
     */
    @Test
    public void setBucketPolicyPublic() {
        String bucketName = "public-bucket";
        // 如果桶不存在，则创建桶
        if (!minioTemplate.bucketExists(bucketName)) {
            minioTemplate.createBucket(bucketName);
        }
        // 存储策略，所有人都有访问权限
        minioTemplate.setBucketAccessPolicy(bucketName, "public");
    }


    /**
     * 设置私有读写访问策略
     */
    @Test
    public void setBucketPolicyPrivate() {
        String bucketName = "private-bucket";
        // 如果桶不存在，则创建桶
        if (!minioTemplate.bucketExists(bucketName)) {
            minioTemplate.createBucket(bucketName);
        }
        // 存储策略，只有拥有者有访问权限
        minioTemplate.setBucketAccessPolicy(bucketName, "private");
    }

    /**
     * 设置桶的自定义访问策略
     */
    @Test
    public void setBucketCustomAccessPolicys() {
        String bucketName = "testbucket";
        // 如果桶不存在，则创建桶
        if (!minioTemplate.bucketExists(bucketName)) {
            minioTemplate.createBucket(bucketName);
        }
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
        minioTemplate.setBucketCustomAccessPolicys(bucketName, policyJson);
    }

    /**
     * 测试允许特定主体对桶内特定对象进行读取
     */
    @Test
    public void testSetBucketCustomAccessPolicyForRead() {
        String bucketName = "test-bucket";
        String objectName = "test.png";
        String principal = "user1"; // 授权给的受信任主体
        List<String> actionList = Arrays.asList("s3:GetObject"); // 仅允许获取对象
        // 创建桶
        minioTemplate.createBucket(bucketName);
        // 设置桶的自定义访问策略
        minioTemplate.setBucketCustomAccessPolicys(bucketName, actionList, principal);

        // 上传一个测试对象
        File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url = minioTemplate.uploadFile(bucketName, objectName, file, "image/png");

        // 根据url解析出目录
        String objectPathFromUrl = minioTemplate.getObjectPathFromUrl(bucketName, url);

        try {
            // 使用授权的受信任主体尝试获取对象
            minioTemplate.downloadFile(bucketName, objectPathFromUrl);
            System.out.println("测试通过");
        } catch (Exception e) {
            System.out.println("测试失败");
        }

        // 打印url测试访问url，发现访问不到图片，因为只设置了获取权限，没有访问权限
        System.out.println(url);
    }

    /**
     * 测试允许特定主体对桶内特定对象进行写入
     */
    @Test
    public void testSetBucketCustomAccessPolicyForWrite() {
        String bucketName = "test-bucket";
        String objectName = "test.png";
        String principal = "user1"; //授权给的受信任主体
        List<String> actionList = Arrays.asList("s3:PutObject"); // 仅允许上传对象

        // 设置桶的自定义访问策略
        minioTemplate.setBucketCustomAccessPolicys(bucketName, actionList, principal);

        // 使用授权的受信任主体尝试上传对象
        try {
            // 上传一个测试对象
            File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
            String url = minioTemplate.uploadFile(bucketName, objectName, file, "image/png");
            System.out.println("测试通过");
            System.out.println(url);
        } catch (Exception e) {
            System.out.println("测试失败");
        }
    }

    /**
     * 测试允许特定主体对桶内所有对象进行读取
     */
    @Test
    public void testSetBucketCustomAccessPolicyForReadAllObjects() {
        String bucketName = "test-bucket";
        String principal = "user1"; // 授权给的受信任主体
        String objectName1 = "test1.png";
        String objectName2 = "test2.png";
        List<String> actionList = Arrays.asList("s3:GetObject"); // 仅允许获取对象

        //设置桶的自定义访问策略
        minioTemplate.setBucketCustomAccessPolicys(bucketName, actionList, principal);

        // 上传一个测试对象，并使用授权的受信任主体尝试获取对象
        File file1 = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url1 = minioTemplate.uploadFile(bucketName, objectName1, file1, "image/png");

        try {
            minioTemplate.downloadFileByUrl(url1);
            System.out.println("测试1通过");
        } catch (Exception e) {
            System.out.println("测试1未通过");
        }

        // 上传另一个测试对象，并使用授权的受信任主体尝试获取对象
        File file2 = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url2 = minioTemplate.uploadFile(bucketName, objectName2, file2, "image/png");
        try {
            minioTemplate.downloadFileByUrl(url2);
            System.out.println("测试2通过");
        } catch (Exception e) {
            System.out.println("测试2未通过");
        }
    }

    /**
     * 测试允许特定主体对桶内所有对象进行写入
     */
    @Test
    public void testSetBucketCustomAccessPolicyForWriteAllObjects() {
        String bucketName = "test-bucket";
        String principal = "user1"; //授权给的受信任主体
        String objectName = "test.png";
        List<String> actionList = Arrays.asList("s3:PutObject"); //仅允许上传对象

        // 设置桶的自定义访问策略
        minioTemplate.setBucketCustomAccessPolicys(bucketName, actionList, principal);

        // 使用授权的受信任主体尝试上传对象
        try {
            File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
            String url = minioTemplate.uploadFile(bucketName, objectName, file, "image/png");
            System.out.println("测试通过");
            System.out.println(url);
        } catch (Exception e) {
            System.out.println("测试失败");
        }
    }

}
