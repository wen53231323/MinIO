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
public class 桶是否存在 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 判断桶是否存在
     */
    @Test
    public void testBucketExists() {
        String bucketName = "testbucket";
        boolean exist = minioTemplate.bucketExists(bucketName);
        if (exist) {
            System.out.println("Bucket桶" + bucketName + "存在!");
        } else {
            System.out.println("Bucket桶" + bucketName + "不存在!");
        }
    }

}
