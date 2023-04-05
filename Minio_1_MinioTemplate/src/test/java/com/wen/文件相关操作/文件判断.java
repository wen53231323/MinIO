package com.wen.文件相关操作;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 2:39
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 文件判断 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 判断MinIO服务器上全局默认存储桶中，指定对象（文件）是否存在
     */
    @Test
    public void checkFolderIsExist1() {
        String objectName = "/2023/04/02/1680433045527-test.png";
        Boolean b = minioTemplate.checkFileIsExist(objectName);
        if (b) {
            System.out.println(objectName + "存在");
        } else {
            System.out.println(objectName + "不存在");
        }
    }

    /**
     * 判断MinIO服务器上指定存储桶中，指定对象（文件）是否存在
     */
    @Test
    void checkFolderIsExist2() {
        String bucketName = "default-bucket";
        String objectName = "/2023/04/02/1680433045527-test.png";
        boolean isExist = minioTemplate.checkFileIsExist(bucketName, objectName);
        if (isExist) {
            System.out.println(objectName + "存在");
        } else {
            System.out.println(objectName + "不存在");
        }
    }
}
