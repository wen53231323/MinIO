package com.wen.文件相关操作.文件上传;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 15:48
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 上传输入流 {
    @Resource
    private MinioTemplate minioTemplate;

    /**
     * 上传输入流到默认文件桶中
     */
    @Test
    void putObjectWithInputStream() throws IOException {
        String objectName = "test.png";
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\音、视、图\\图片\\静态图片\\2.png"));
        String url = minioTemplate.uploadFile(objectName, fileInputStream, "image/png");
        System.out.println(url);
    }

    /**
     * 上传输入流到指定的文件桶中
     */
    @Test
    void putObjectWithBucketAndInputStream() throws IOException {
        String bucketName = "test-bucket";
        String objectName = "test.png";
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\音、视、图\\图片\\静态图片\\2.png"));
        String url = minioTemplate.uploadFile(bucketName, objectName, fileInputStream , "image/png");
        System.out.println(url);
    }
}
