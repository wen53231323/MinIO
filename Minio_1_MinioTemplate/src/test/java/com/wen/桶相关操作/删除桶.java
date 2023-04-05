package com.wen.桶相关操作;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 0:12
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 删除桶 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 删除单个空桶，根据存储桶名称删除文件为空的桶（桶内无文件）
     */
    @Test
    public void testRemoveBucket() {
        String bucketName = "testbucket";
        // 判断桶是否存在
        boolean exist = minioTemplate.bucketExists(bucketName);
        if (exist) {
            minioTemplate.removeBucket(bucketName);
            System.out.println("Bucket桶" + bucketName + "删除成功!");
        } else {
            System.out.println("Bucket桶" + bucketName + "删除失败!");
        }
    }

    /**
     * 删除多个空桶，根据存储桶名称列表，依次删除多个文件为空的桶（桶内无文件）
     */
    @Test
    public void testRemoveBucketList() {
        // 获取所有桶名称列表
        List<String> bucketNameList = minioTemplate.listBuckets();

        System.out.println(bucketNameList);
        if (bucketNameList.size() > 0) {
            minioTemplate.removeBucketList(bucketNameList);
            System.out.println("Bucket桶" + bucketNameList + "删除成功!");
        } else {
            System.out.println("Bucket桶" + bucketNameList + "删除失败!");
        }
    }
}
