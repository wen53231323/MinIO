package com.wen.桶相关操作;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import io.minio.messages.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 0:20
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 获取桶信息 {

    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 获取所有的桶信息
     */
    @Test
    public void testGetAllBuckets() {
        // 获取所有的桶信息
        List<Bucket> buckets = minioTemplate.getAllBuckets();
        buckets.forEach(System.out::println);
    }

    /**
     * 根据bucketName桶名称获取桶信息
     */
    @Test
    public void testGetBucket() {
        // 根据bucketName桶名称获取信息
        Optional<Bucket> optional = minioTemplate.getBucket("test");
        System.out.println(optional);
        // 打印桶是否存在以及桶对象信息
        System.out.println(optional.isPresent());
        if (optional.isPresent()) {
            System.out.println(optional.get());
        } else {
            System.out.println("桶不存在");
        }
    }

    /**
     * 获取所有桶名称列表
     */
    @Test
    public void testGetAllBucket() {
        List<String> nameList = minioTemplate.listBuckets();
        nameList.forEach(System.out::println);
    }

    /**
     * 获取指定桶的访问策略
     */
    @Test
    public void testGetBucketAccessPolicy() {
        String bucketName = "public-bucket";
        String accessPolicy = minioTemplate.getBucketAccessPolicy(bucketName);
        if (accessPolicy != null && !accessPolicy.isEmpty()) {
            System.out.println("Bucket桶" + bucketName + "访问策略: " + accessPolicy);
        } else {
            System.out.println("获取Bucket桶" + bucketName + "访问策略失败!");
        }
    }
}
