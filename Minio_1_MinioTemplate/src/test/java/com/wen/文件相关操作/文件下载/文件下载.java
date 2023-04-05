package com.wen.文件相关操作.文件下载;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 2:38
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 文件下载 {
    @Resource
    private MinioTemplate minioTemplate;

    /**
     * 下载文件，根据URL获取文件流
     */
    @Test
    public void downloadFileByUrlTest() throws IOException {
        //获取文件流
        String url = "http://localhost:9000/default-bucket/2023/04/02/1680425149113-test.png";
        InputStream is = minioTemplate.downloadFileByUrl(url);
        //将文件流保存至本地
        FileOutputStream fos = new FileOutputStream("E:/test/test.png");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        System.out.println("下载成功");
    }

    /**
     * 下载文件，根据文件全路径从全局默认桶获取文件流
     */
    @Test
    public void downloadFileTest1() throws IOException {
        //获取文件流
        String objectName = "2023/04/02/1680425149113-test.png";
        InputStream is = minioTemplate.downloadFile(objectName);
        //将文件流保存至本地
        FileOutputStream fos = new FileOutputStream("E:/test/test.png");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        System.out.println("下载成功");
    }

    /**
     * 下载文件，据文件桶和文件全路径获取文件流
     */
    @Test
    public void downloadFileTest2() throws IOException {
        //获取文件流
        String bucketName = "default-bucket";
        String objectName = "2023/04/02/1680425149113-test.png";
        InputStream is = minioTemplate.downloadFile(bucketName, objectName);
        //将文件流保存至本地
        FileOutputStream fos = new FileOutputStream("E:/test/test.png");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        System.out.println("下载成功");
    }

    /**
     * 下载文件，根据URL获取字节数组
     */
    @Test
    public void downloadFileByteByUrlTest() throws IOException {
        //获取字节数组
        String url = "http://localhost:9000/default-bucket/2023/04/02/1680425149113-test.png";
        byte[] data = minioTemplate.downloadFileByteByUrl(url);
        //将字节数组保存至本地
        FileOutputStream fos = new FileOutputStream("E:/test/test.png");
        fos.write(data);
        fos.close();
        System.out.println("下载成功");
    }

    /**
     * 下载文件，根据文件全路径从全局默认桶获取字节数组
     */
    @Test
    public void downloadFileByteTest1() throws IOException {
        //获取字节数组
        String objectName = "2023/04/02/1680425149113-test.png";
        byte[] data = minioTemplate.downloadFileByte(objectName);
        //将字节数组保存至本地
        FileOutputStream fos = new FileOutputStream("E:/test/test.png");
        fos.write(data);
        fos.close();
        System.out.println("下载成功");
    }

    /**
     * 下载文件，据文件桶和根据文件全路径获取字节数组
     */
    @Test
    public void downloadFileByteTest2() throws IOException {
        //获取字节数组
        String bucketName = "default-bucket";
        String objectName = "2023/04/02/1680425149113-test.png";
        byte[] data = minioTemplate.downloadFileByte(bucketName, objectName);
        //将字节数组保存至本地
        FileOutputStream fos = new FileOutputStream("E:/test/test.png");
        fos.write(data);
        fos.close();
        System.out.println("下载成功");
    }


}
