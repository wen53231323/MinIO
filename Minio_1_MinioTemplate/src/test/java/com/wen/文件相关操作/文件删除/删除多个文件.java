package com.wen.文件相关操作.文件删除;

import com.wen.MinIOApplication;
import com.wen.service.MinioTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/2 19:03
 */
@SpringBootTest(classes = {MinIOApplication.class})
public class 删除多个文件 {
    @Autowired
    private MinioTemplate minioTemplate;
    /**
     * 删除多个文件，删除全局默认桶中的多个文件（列表中的所有文件）
     */
    @Test
    public void removeObjectList1() {
        ArrayList<String> nameList = new ArrayList<>();
        nameList.add("2023/04/02/1680425168425-test.png");
        nameList.add("2023/04/02/1680425260618-test.png");
        nameList.add("2023/04/02/1680425272027-test.png");
        boolean isDeleted = minioTemplate.removeObjectList(nameList);
        if (isDeleted) {
            System.out.println("文件删除成功");
        } else {
            System.out.println("文件删除失败");
        }
    }

    /**
     * 删除多个文件，删除指定桶中的多个文件（列表中的所有文件）
     */
    @Test
    public void removeObjectList2() {
        String bucketName = "test-bucket";
        ArrayList<String> nameList = new ArrayList<>();
        nameList.add("2023/04/02/1680425168425-test.png");
        nameList.add("2023/04/02/1680425260618-test.png");
        nameList.add("2023/04/02/1680425272027-test.png");
        boolean isDeleted = minioTemplate.removeObjectList(bucketName, nameList);
        if (isDeleted) {
            System.out.println("文件删除成功");
        } else {
            System.out.println("文件删除失败");
        }
    }
}
