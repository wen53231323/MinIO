package com.wen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wen.pojo.PageResult;
import com.wen.pojo.RestResponse;
import com.wen.pojo.dto.QueryMediaParamsDto;
import com.wen.pojo.dto.UploadFileDTO;
import com.wen.pojo.entity.MediaFiles;
import com.wen.pojo.vo.UploadFilesVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 通用小文件接口
 */
public interface MediaFilesService extends IService<MediaFiles> {
    /**
     * 根据条件查询数据
     *
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     */
    PageResult<MediaFiles> queryMediaFiles(QueryMediaParamsDto queryMediaParamsDto);

    /**
     * TODO 上传普通文件通用接口
     *
     * @param filedata 上传的文件，SpringMVC中将上传的文件封装到MultipartFile对象中，通过此对象可以获取文件相关信息
     */
    MediaFiles uploadFile(MultipartFile filedata);

    /**
     * TODO 将文件信息存入数据库
     *
     * @param userId         机构id
     * @param fileId         文件的md5值
     * @param uploadFilesDTO 上传文件的文件信息
     * @param bucket         桶
     * @param objectName     对象名称（目录+文件名）
     * @param url            文件URL
     */
    @Transactional
    MediaFiles addMediaFilesToDb(Long userId, String fileId, UploadFileDTO uploadFilesDTO, String bucket, String objectName, String url);














    /**
     * 根据文件ID获取文件信息
     *
     * @param mediaId 文件ID
     * @return
     */
    MediaFiles getFileById(String mediaId);




}

