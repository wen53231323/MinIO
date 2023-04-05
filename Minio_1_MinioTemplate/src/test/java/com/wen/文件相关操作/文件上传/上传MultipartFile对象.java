package com.wen.文件相关操作.文件上传;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author wen
 * @version 1.0
 * @description TODO 文件上传测试
 * @date 2023/4/1 18:07
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 上传MultipartFile对象 {
    @Resource
    private MinioTemplate minioTemplate;

    /**
     * 上传MultipartFile对象到默认文件桶中
     */
    @Test
    void putObjectWithMultipartFile() throws IOException {
        //（1）获取 MockMultipartFile 对象
        MockMultipartFile imgMultipartFile = getImgMultipartFile();
        MockMultipartFile txtMultipartFile = getTxtMultipartFile();

        //（3）上传文件
        String url1 = minioTemplate.uploadFile(imgMultipartFile);
        String url2 = minioTemplate.uploadFile(txtMultipartFile);
        System.out.println(url1);
        System.out.println(url2);
    }

    /**
     * 上传MultipartFile对象到指定的文件夹下
     */
    @Test
    void putObjectWithObjectNameAndMultipartFile() throws IOException {
        //（1）获取 MockMultipartFile 对象
        MockMultipartFile imgMultipartFile = getImgMultipartFile();
        MockMultipartFile txtMultipartFile = getTxtMultipartFile();

        //（3）上传文件
        String objectName1 = "imgs/test.png";
        String objectName2 = "txt/test.txt";
        String url = minioTemplate.uploadFile(objectName1, imgMultipartFile);
        String ur2 = minioTemplate.uploadFile(objectName2, txtMultipartFile);
        System.out.println(url);
        System.out.println(ur2);
    }

    /**
     * 上传MultipartFile对象到指定的文件桶中
     */
    @Test
    void putObjectWithBucketAndObjectNameAndMultipartFile() throws IOException {
        //（1）获取 MockMultipartFile 对象
        MockMultipartFile imgMultipartFile = getImgMultipartFile();
        MockMultipartFile txtMultipartFile = getTxtMultipartFile();

        //（2）上传文件
        String bucketName = "test-bucket";
        String objectName1 = "imgs/test.png";
        String objectName2 = "txt/test.txt";
        String url = minioTemplate.uploadFile(bucketName, objectName1, imgMultipartFile);
        String ur2 = minioTemplate.uploadFile(bucketName, objectName2, txtMultipartFile);
        System.out.println(url);
        System.out.println(ur2);
    }

    /**
     * 使用文件路径创建 MockMultipartFile 对象
     *
     * @return MockMultipartFile 对象
     */
    public static MockMultipartFile getImgMultipartFile() throws IOException {
        Path path = Paths.get("E:\\音、视、图\\图片\\静态图片\\2.png");
        byte[] content = Files.readAllBytes(path);
        MockMultipartFile file = new MockMultipartFile(
                "file",                   // 文件域的名称
                "test.png",      // 文件名
                "image/png",        // 文件类型
                content                         // 文件内容
        );
        return file;
    }

    /**
     * 使用字节数组创建 MockMultipartFile 对象
     *
     * @return MockMultipartFile 对象
     */
    public static MockMultipartFile getTxtMultipartFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",                   // 文件域的名称
                "test.txt",      // 文件名
                "text/plain",       // 文件类型
                "Hello World".getBytes()        // 文件内容
        );
        return file;
    }

}
