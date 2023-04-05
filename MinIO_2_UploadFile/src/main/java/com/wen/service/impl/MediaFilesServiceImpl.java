package com.wen.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.wen.mapper.MediaFilesMapper;
import com.wen.mapper.MediaProcessMapper;
import com.wen.pojo.PageResult;
import com.wen.pojo.RestResponse;
import com.wen.pojo.SystemException;
import com.wen.pojo.dto.QueryMediaParamsDto;
import com.wen.pojo.dto.UploadFileDTO;
import com.wen.pojo.entity.MediaFiles;
import com.wen.pojo.entity.MediaProcess;
import com.wen.pojo.entity.ResponseResult;
import com.wen.pojo.vo.UploadFilesVO;
import com.wen.service.MediaFilesService;
import com.wen.service.MinioTemplate;
import com.wen.utils.IdWorkerUtils;
import io.minio.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 通用文件接口实现类
 */
@Slf4j
@Service("mediaFilesService")
public class MediaFilesServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFilesService {

    // 普通文件存储的桶
    @Value("${minio.bucket.mediafiles}")
    private String bucket_files;

    @Resource
    private MediaFilesService mediaFilesService;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MinioClient minioClient;

    @Resource
    private MinioTemplate minioTemplate;


    @Value("${minio.bucket.videofiles}")
    private String video_files;

    /**
     * 根据条件查询数据
     *
     * @param queryMediaParamsDto 查询条件
     * @return
     */
    @Override
    public PageResult<MediaFiles> queryMediaFiles(QueryMediaParamsDto queryMediaParamsDto) {
        Long pageNo = queryMediaParamsDto.getPageNo();
        Long pageSize = queryMediaParamsDto.getPageSize();
        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(queryMediaParamsDto.getName()), MediaFiles::getFileName, queryMediaParamsDto.getName());
        queryWrapper.eq(!StringUtils.isEmpty(queryMediaParamsDto.getType()), MediaFiles::getFileType, queryMediaParamsDto.getType());
        // 分页对象
        Page<MediaFiles> page = new Page<>(pageNo, pageSize);
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageNo, pageSize);
        return mediaListResult;
    }

    @Override
    public MediaFiles uploadFile(MultipartFile filedata) {
        Long userId = 1232141425L;// 用户id
        UploadFileDTO uploadFilesDTO = new UploadFileDTO();
        // 如果 filedata.getContentType() 返回值为 null，则返回默认值 MediaType.APPLICATION_OCTET_STREAM_VALUE 未知的二进制流
        String contentType = Optional.ofNullable(filedata.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        uploadFilesDTO.setContentType(contentType);// 设置文件类型
        uploadFilesDTO.setFileSize(filedata.getSize());// 设置文件大小
        uploadFilesDTO.setFileName(filedata.getOriginalFilename());// 设置文件名称
        uploadFilesDTO.setFileTag("课程视频");
        // 判断文件类型并设置（文件类型：图片,文档,视频,音乐,其他）
        if (filedata.getContentType().indexOf("image") >= 0) {
            uploadFilesDTO.setFileType("图片");
        } else if (filedata.getContentType().indexOf("document") >= 0) {
            uploadFilesDTO.setFileType("文档");
        } else if (filedata.getContentType().indexOf("video") >= 0) {
            uploadFilesDTO.setFileType("视频");
        } else if (filedata.getContentType().indexOf("audio") >= 0) {
            uploadFilesDTO.setFileType("音乐");
        } else{
            uploadFilesDTO.setFileType("其他");
        }

        // 获取文件的md5值（MD5不是加密算法，是一段摘要算法，生成固定长度的串，不可逆）
        String fileMd5 = null;
        try {
            // 计算文件的MD5值
            fileMd5 = DigestUtil.md5Hex(IoUtil.toStream(filedata.getBytes()));
        } catch (Exception e) {
            log.error("获取文件的md5值出错：{}", e.getMessage());
            ResponseResult.errorResult(400, "获取文件内容出错");
        }

        // 如果对象名称objectName（目录+文件名）为空，则使用 文件目录 拼接 文件md5值 获取objectName对象名称
        String filename = uploadFilesDTO.getFileName();// 获取文件名称

        try {
            // 上传到 minio 返回URL
            String url = minioTemplate.uploadFile(bucket_files, filename, filedata);
            // 根据 URL 解析出指定桶的文件目录
            String objectName = minioTemplate.getObjectPathByUrl(bucket_files, url);
            // 将文件信息存入数据库
            MediaFiles mediaFiles = mediaFilesService.addMediaFilesToDb(userId, fileMd5, uploadFilesDTO, bucket_files, objectName, url);
            return mediaFiles;
        } catch (Exception e) {
            log.error("上传文件到minio出错：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 将文件信息添加到文件表
     *
     * @param userId         机构id
     * @param fileMd5        文件的md5码
     * @param uploadFilesDTO 上传文件的文件信息
     * @param bucket         桶
     * @param objectName     对象名称（目录+文件名）
     * @return
     */
    @Transactional // 开启事务
    @Override
    public MediaFiles addMediaFilesToDb(Long userId, String fileMd5, UploadFileDTO uploadFilesDTO, String bucket, String objectName, String url) {
        // 根据文件的md5值查询媒资数据库中是否有数据（默认以md5值为主键）
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            try {
                mediaFiles = new MediaFiles();
                // 封装数据
                BeanUtils.copyProperties(uploadFilesDTO, mediaFiles);
                mediaFiles.setFileId(IdWorkerUtils.getInstance().nextId()); // 文件ID（使用雪花算法）
                mediaFiles.setUserId(userId);// 用户id
                mediaFiles.setFileBucket(bucket); // 桶
                mediaFiles.setFilePath(objectName); // 路径+文件名
                mediaFiles.setFileUrl(url);// 文件url
                mediaFiles.setFileStatus(1); // 状态正常
                mediaFiles.setFileMd5(fileMd5);// 文件md5
                mediaFiles.setAuditStatus("3"); // 审核状态
                mediaFiles.setIsDisplay(1);// 是否展示（0表示不展示，1表示展示）
                mediaFiles.setCreateTime(LocalDateTime.now());
                mediaFiles.setUpdateTime(LocalDateTime.now());
                mediaFiles.setAuditMind("");// 审核意见

                // 保存文件信息到媒资数据库
                int insert = mediaFilesMapper.insert(mediaFiles);
                if (insert <= 0) {
                    log.error("将文件信息存入数据库出错");
                    throw new RuntimeException("将文件信息存入数据库出错");
                }
                // 如果是avi视频，则额外添加至视频待处理表
                if ("video/x-msvideo".equals(uploadFilesDTO.getContentType())) {
                    MediaProcess mediaProcess = new MediaProcess();
                    BeanUtils.copyProperties(mediaFiles, mediaProcess);
                    mediaProcess.setStatus("1"); // 未处理
                    int processInsert = mediaProcessMapper.insert(mediaProcess);
                    if (processInsert <= 0) {
                        log.error("将文件信息存入数据库出错");
                        throw new RuntimeException("将文件信息存入数据库出错");
                    }
                }
            } catch (Exception e) {
                log.error("将文件信息存入数据库出错:{}", e.getMessage());
                throw new RuntimeException("将文件信息存入数据库出错");
            }
        }
        return mediaFiles;
    }

    /**
     * 根据日期生成文件夹路径
     *
     * @param date         日期对象，不能为空
     * @param includeYear  是否包含年份
     * @param includeMonth 是否包含月份
     * @param includeDay   是否包含天数
     * @return 文件夹路径，格式如：“2023/04/03/”
     * @throws IllegalArgumentException 如果日期对象为空，则抛出该异常
     */
    public static String getFileFolder(Date date, boolean includeYear, boolean includeMonth, boolean includeDay) {
        // 判断日期对象是否为空，空则抛出异常
        Objects.requireNonNull(date, "日期对象不能为空");

        // 将日期对象转换为LocalDate对象
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 根据参数includeYear、includeMonth、includeDay判断是否需要年、月、日，需要则获取相应的值。
        int year = includeYear ? localDate.getYear() : 0;
        int month = includeMonth ? localDate.getMonthValue() : 0;
        int day = includeDay ? localDate.getDayOfMonth() : 0;

        // 新建StringBuilder对象，用于拼接文件夹路径
        StringBuilder folderPath = new StringBuilder();
        if (year > 0) {
            // 如果需要年份，则将年份加入到文件夹路径中，并在后面添加“/”
            folderPath.append(year).append("/");
        }
        if (month > 0) {
            // 如果需要月份，则将月份加入到文件夹路径中，并在后面添加“/”
            folderPath.append(String.format("%02d", month)).append("/");
        }
        if (day > 0) {
            // 如果需要天数，则将天数加入到文件夹路径中，并在后面添加“/”
            folderPath.append(String.format("%02d", day)).append("/");
        }

        // 将StringBuilder对象转换为字符串并返回
        return folderPath.toString();
    }

    @Override
    public MediaFiles getFileById(String id) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(id);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getFileUrl())) {
            throw new SystemException(400, "视频还没有转码处理");
        }
        return mediaFiles;
    }

}

