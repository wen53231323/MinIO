package com.wen.文件相关操作.文件上传;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 15:46
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 上传字节数组 {
    @Resource
    private MinioTemplate minioTemplate;
    /**
     * 上传字节数组到默认文件桶中
     */
    @Test
    void putObjectWithBytes() throws IOException {
        String objectName = "test.png";
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\音、视、图\\图片\\静态图片\\2.png"));

        // 使用 ByteArrayOutputStream 将文件数据写入内存缓冲区，并转换为 byte[] 数组
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = fileInputStream.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] bytes = output.toByteArray();

        // 调用 MinIO 客户端的 putObject 方法将 byte[] 数组上传到指定的存储桶
        String url = minioTemplate.uploadFile(objectName, bytes, "image/png");
        System.out.println(url);
    }

    /**
     * 上传字节数组到指定的文件桶中
     */
    @Test
    void putObjectWithBucketAndBytes() throws IOException {
        String bucketName = "test-bucket";
        String objectName = "test.png";
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\音、视、图\\图片\\静态图片\\2.png"));

        // 使用 ByteArrayOutputStream 将文件数据写入内存缓冲区，并转换为 byte[] 数组
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = fileInputStream.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] bytes = output.toByteArray();

        // 调用 MinIO 客户端的 putObject 方法将 byte[] 数组上传到指定的存储桶
        String url = minioTemplate.uploadFile(bucketName, objectName, bytes, "image/png");
        System.out.println(url);
    }

}
