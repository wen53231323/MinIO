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
 * @date 2023/4/2 19:02
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 删除单个文件 {
    @Autowired
    private MinioTemplate minioTemplate;

    /**
     * 删除单个文件，删除全局默认桶中的文件
     */
    @Test
    public void removeObject1() {

        String objectName = "2023/04/02/1680430936646-test.png";
        boolean isDeleted = minioTemplate.removeObject(objectName);
        if (isDeleted) {
            System.out.println("文件删除成功");
        } else {
            System.out.println("文件删除失败");
        }
    }

    /**
     * 删除单个文件，删除指定桶中的文件
     */
    @Test
    public void removeObject2() {
        String bucketName = "default-bucket";
        String objectName = "2023/04/02/1680430528085-test.png";
        boolean isDeleted = minioTemplate.removeObject(bucketName, objectName);
        if (isDeleted) {
            System.out.println("文件删除成功");
        } else {
            System.out.println("文件删除失败");
        }
    }

}
