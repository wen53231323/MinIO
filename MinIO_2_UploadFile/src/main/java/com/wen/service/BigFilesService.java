package com.wen.service;

import com.wen.pojo.RestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author wen
 * @version 1.0
 * @description TODO 大文件通用接口
 * @date 2023/4/4 12:34
 */
public interface BigFilesService {
    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return 存在true、反之false
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的MD5
     * @param chunkIndex 分块序号
     * @return 存在true、反之false
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     *
     * @param fileMd5 文件MD5
     * @param chunkIndex   分块序号
     * @param file    MultipartFile文件
     * @return 存在true、反之false
     */
    RestResponse uploadChunk(String fileMd5, int chunkIndex, MultipartFile file);

    /**
     * 合并分块
     *
     * @param fileMd5    文件MD5
     * @param fileName   文件名称
     * @param chunkTotal 分块数量
     * @return 存在true、反之false
     */
    RestResponse mergeChunks(String fileMd5, String fileName, int chunkTotal) throws IOException;


    /**
     * 根据文件md5，生成在minio中的文件路径
     *
     * @param fileMd5   文件md5
     * @param extension 文件后缀名
     * @return
     */
    String getFilePathByMd5(String fileMd5, String extension);


}
