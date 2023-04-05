package com.wen.文件相关操作.文件删除;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 2:38
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 删除目录及文件 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 删除全局默认桶内的 文件夹及 文件夹内的所有文件
     */
    @Test
    public void removeAllObject1() {
        String directory = "2023";
        // 调用方法删除文件
        boolean b = minioTemplate.removeAllObject(directory);
        if (b) {
            System.out.println("删除成功");
        } else {
            System.out.println("删除失败");
        }
    }

    /**
     * 删除指定桶内的 文件夹及 文件夹内的所有文件
     */
    @Test
    public void removeAllObject2() {
        String bucketName = "test-bucket";
        String directory = "2023/04/";
        // 调用方法删除文件
        boolean b = minioTemplate.removeAllObject(bucketName, directory);
        if (b) {
            System.out.println("删除成功");
        } else {
            System.out.println("删除失败");
        }
    }
}
