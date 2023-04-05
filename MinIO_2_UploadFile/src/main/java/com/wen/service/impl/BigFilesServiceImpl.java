package com.wen.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.wen.mapper.MediaFilesMapper;
import com.wen.pojo.RestResponse;
import com.wen.pojo.SystemException;
import com.wen.pojo.dto.UploadFileDTO;
import com.wen.pojo.entity.MediaFiles;
import com.wen.service.BigFilesService;
import com.wen.service.MediaFilesService;
import com.wen.service.MinioTemplate;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/4/4 12:34
 */
@Slf4j
@Service
public class BigFilesServiceImpl implements BigFilesService {
    @Resource
    private MediaFilesService mediaFilesService;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MinioTemplate minioTemplate;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.videofiles}")
    private String video_files;

    // 普通文件存储的桶
    @Value("${minio.bucket.mediafiles}")
    private String bucket_files;

    /**
     * 判断文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 根据文件md5查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectByMd5(fileMd5);

        // 数据库中不存在，则直接返回false 表示不存在
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }

        // 若数据库中存在，根据数据库中的文件信息，则继续判断bucket中是否存在
        try {
            Boolean isExist = minioTemplate.checkFileIsExist(mediaFiles.getFileBucket(), mediaFiles.getFilePath());
            return RestResponse.success(isExist);
        } catch (Exception e) {
            log.error("检查文件是否存在异常：{}", e.getMessage());
            return RestResponse.success(false);
        }
    }

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的MD5
     * @param chunkIndex 分块序号
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 获取分块目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        try {
            // 检查是否存在
            Boolean isExist = minioTemplate.checkFileIsExist(video_files, chunkFilePath);
            return RestResponse.success(isExist);
        } catch (Exception e) {
            log.debug("检查分块是否存在异常：{}", e.getMessage());
            return RestResponse.success(false);
        }
    }

    /**
     * 上传分块到视频桶
     *
     * @param fileMd5    文件MD5
     * @param chunkIndex 分块序号
     * @param file       MultipartFile文件
     * @return
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunkIndex, MultipartFile file) {
        // 分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunkIndex;
        try {
            minioTemplate.putMultipartFile(video_files, chunkFilePath, file);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.debug("上传分块文件：{}失败：{}", chunkFilePath, e.getMessage());
        }
        return RestResponse.validfail("上传文件失败", false);
    }

    /**
     * 合并分块
     *
     * @param fileMd5    文件MD5
     * @param fileName   文件名称
     * @param chunkTotal 分块数量
     * @return 存在true、反之false
     */
    @Override
    public RestResponse mergeChunks(String fileMd5, String fileName, int chunkTotal) {
        try {
            // 下载分块文件并写入本地临时文件----------------------------------
            // 获取分块文件夹路径
            String chunkFileFolder = getChunkFileFolderPath(fileMd5);
            // 从MinIO对象存储中下载分块文件，然后将其写入本地临时文件，并返回临时文件数组
            File[] files = minioTemplate.downloadChunkFile(video_files, chunkFileFolder, chunkTotal);

            // 合并临时分块文件为临时文件-----------------------------------
            File mergeFile = mergeFiles(files);

            // 校验文件MD5值是否匹配-------------------------------
            verifyFileMd5(fileMd5, mergeFile);

            // 上传文件-----------------------------------------
            String absolutePath = mergeFile.getAbsolutePath();// 获取文件的绝对路径
            String contentType = minioTemplate.getContentType(fileName);// 获取contentType
            String url = minioTemplate.putLocalFile(absolutePath, video_files, fileName, contentType);// 将合并好的文件存入minio并获取url
            String objectPath = minioTemplate.getObjectPathByUrl(video_files, url);// 文件存储目录

            // 将文件信息写入数据库------------------------------------------------------
            Long userId = 1232141425L;
            UploadFileDTO uploadFilesDTO = new UploadFileDTO();
            uploadFilesDTO.setUserName("小明");
            uploadFilesDTO.setFileType("视频");
            uploadFilesDTO.setFileTag("课程视频");
            uploadFilesDTO.setRemark("");
            uploadFilesDTO.setFileName(fileName);
            uploadFilesDTO.setContentType(contentType);
            uploadFilesDTO.setFileSize(mergeFile.length());// 设置文件大小
            MediaFiles mediaFiles = mediaFilesService.addMediaFilesToDb(userId, fileMd5, uploadFilesDTO, video_files, objectPath, url);
            if (mediaFiles == null) {
                throw new SystemException(400, "媒资文件入库出错");
            }

            // 清理minio中的分块文件、清理临时分块文件数组、清理临时合并文件-----------------------------------------------------
            clearChunkFilesAndLocalTempFiles(video_files, chunkFileFolder, chunkTotal, files, mergeFile);
            return RestResponse.success();
        } catch (Exception e) {
            log.error("合并视频文件失败：{}", e.getMessage());
            throw new SystemException(400, "合并视频文件失败");
        }
    }

    /**
     * 合并临时文件
     *
     * @param files 待合并的临时文件
     * @return 合并后的完整文件
     * @throws SystemException 合并文件失败时抛出异常
     */
    public static File mergeFiles(File[] files) throws SystemException {
        // 创建合并临时文件
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile("prefix", ".test");
        } catch (IOException e) {
            log.debug("创建合并临时文件出错：{}", e.getMessage());
            throw new SystemException(400, "创建合并临时文件出错");
        }

        // 写入流，向临时文件写入
        try (FileChannel fileChannel = new FileOutputStream(mergeFile, true).getChannel()) {
            // 缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 遍历分块文件列表
            for (File file : files) {
                // 读取流，读分块文件
                try (FileChannel inChannel = new FileInputStream(file).getChannel()) {
                    while (inChannel.read(buffer) > 0) {
                        buffer.flip();
                        fileChannel.write(buffer);
                        buffer.clear();
                    }
                } catch (IOException e) {
                    log.debug("读取分块文件出错：{}", e.getMessage());
                    throw new SystemException(400, "读取分块文件出错");
                }
            }
        } catch (IOException e) {
            log.debug("合并文件过程中出错：{}", e.getMessage());
            throw new SystemException(400, "合并文件过程中出错");
        }
        return mergeFile;
    }

    /**
     * 校验文件MD5值是否匹配
     *
     * @param fileMd5    文件MD5值
     * @param verifyFile 待校验的文件
     * @throws SystemException 校验失败时抛出异常
     */
    public static void verifyFileMd5(String fileMd5, File verifyFile) throws SystemException {
        try (FileInputStream mergeInputStream = new FileInputStream(verifyFile)) {
            // 生成md5
            String mergeMd5 = MD5.create().digestHex(mergeInputStream);
            if (!fileMd5.equals(mergeMd5)) {
                throw new SystemException(400, "合并文件校验失败");
            }
            log.debug("合并文件校验通过：{}", verifyFile.getAbsolutePath());
        } catch (IOException e) {
            log.debug("合并文件校验异常：{}", e.getMessage());
            throw new SystemException(400, "合并文件校验异常");
        }
    }

    /**
     * 清理minio中的分块文件、清理临时分块文件数组、清理临时合并文件
     *
     * @param bucketName 文件桶名称
     * @param path       分块文件路径
     * @param chunkTotal 分块文件总数
     * @param files      临时文件数组
     * @param mergeFile  临时合并文件
     */
    private void clearChunkFilesAndLocalTempFiles(String bucketName, String path, int chunkTotal, File[] files, File mergeFile) {
        // 清理MinIO中的分块文件
        try {
            minioTemplate.clearChunkFiles(bucketName, path, chunkTotal);
        } catch (Exception e) {
            log.debug("清理分块文件失败：{}", e.getMessage());
        }
        // 删除本地临时文件
        for (File file : files) {
            try {
                file.delete();
            } catch (Exception e) {
                log.debug("删除临时分块文件错误：{}", e.getMessage());
            }
        }
        try {
            mergeFile.delete();
        } catch (Exception e) {
            log.debug("删除临时合并文件错误：{}", e.getMessage());
        }
    }


    /**
     * 根据MD5和文件扩展名，生成文件路径，例 /2/f/2f6451sdg/2f6451sdg.mp4
     *
     * @param fileMd5   文件MD5
     * @param extension 文件扩展名
     * @return
     */
    @Override
    public String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }

    /**
     * 获取分块文件的目录
     * 例如文件的md5码为  1f2465f， 那么该文件的分块放在 /1/f/1f2465f下，即前两级目录为md5的前两位
     *
     * @param fileMd5 文件md5
     * @return 分块文件目录（md5前两位拼接）
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

}
