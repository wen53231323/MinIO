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
 * @date 2023/4/2 19:44
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 文件名获取 {
    @Autowired
    private MinioTemplate minioTemplate;

    @Test
    public void testGetObjectNameByUrl() {
        String url = "http://192.168.199.183:9000/default-bucket/2023/04/02/1680433045527-test.png";
        String objectName = minioTemplate.getObjectNameByUrl(url);
        System.out.println(objectName);
    }
}
