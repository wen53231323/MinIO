package com.wen.controller;


import com.wen.pojo.RestResponse;
import com.wen.service.BigFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 大文件分块上传接口
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFilesController {

    @Resource
    private BigFilesService bigFilesService;

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return 存在true、反之false
     */
    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return bigFilesService.checkFile(fileMd5);
    }

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的MD5
     * @param chunkIndex 分块序号
     * @return 存在true、反之false
     */
    @ApiOperation(value = "分块文件上传前检查分块")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunkIndex") int chunkIndex) {
        return bigFilesService.checkChunk(fileMd5, chunkIndex);
    }

    /**
     * 上传分块
     *
     * @param fileMd5 文件的MD5
     * @param chunk   分块序号
     * @param file    MultipartFile文件
     * @return 存在true、反之false
     */
    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") MultipartFile file, @RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) throws Exception {
        return bigFilesService.uploadChunk(fileMd5, chunk, file);
    }

    /**
     * 合并分块
     *
     * @param fileMd5    文件MD5
     * @param fileName   文件名称
     * @param chunkTotal 分块数量
     * @return 存在true、反之false
     */
    @ApiOperation(value = "合并分块文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5, @RequestParam("fileName") String fileName, @RequestParam("chunkTotal") int chunkTotal) throws IOException {
        return bigFilesService.mergeChunks(fileMd5, fileName, chunkTotal);
    }
}
