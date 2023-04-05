package com.wen.文件相关操作.文件上传;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 15:32
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 上传File对象 {
    @Resource
    private MinioTemplate minioTemplate;

    /**
     * 上传File对象到默认文件桶中
     */
    @Test
    void putObjectWithFile() {
        String objectName = "test.png";
        File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url = minioTemplate.uploadFile(objectName, file, "image/png");
        System.out.println(url);
    }

    /**
     * 上传File对象到指定的文件桶中
     */
    @Test
    void putObjectWithBucketAndFile() {
        String bucketName = "test-bucket";
        String objectName = "test.png";
        File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url = minioTemplate.uploadFile(bucketName, objectName, file, "image/png");
        System.out.println(url);
    }

}
