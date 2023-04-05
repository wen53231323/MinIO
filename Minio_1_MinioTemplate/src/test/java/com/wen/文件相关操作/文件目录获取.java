package com.wen.文件相关操作;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 23:59
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 文件目录获取 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 根据 URL 解析出全局默认桶文件目录
     */
    @Test
    public void getObjectPathFromUrl1() {
        // 上传一个测试对象
        String objectName = "test.png";
        File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url = minioTemplate.uploadFile(objectName, file, "image/png");
        System.out.println(url);

        // 根据url解析出目录
        String objectPathFromUrl = minioTemplate.getObjectPathFromUrl(url);
        System.out.println(objectPathFromUrl);
    }

    /**
     * 根据 URL 解析出指定桶的文件目录
     */
    @Test
    public void getObjectPathFromUrl2() {
        // 上传一个测试对象
        String bucketName = "test-bucket";
        String objectName = "test.png";
        File file = new File("E:\\音、视、图\\图片\\静态图片\\2.png");
        String url = minioTemplate.uploadFile(bucketName, objectName, file, "image/png");
        System.out.println(url);

        // 根据url解析出目录
        String objectPathFromUrl = minioTemplate.getObjectPathFromUrl(bucketName, url);
        System.out.println(objectPathFromUrl);
    }
}
