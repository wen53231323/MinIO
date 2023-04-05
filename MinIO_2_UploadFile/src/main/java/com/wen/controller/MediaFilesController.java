package com.wen.controller;

import com.wen.pojo.PageResult;
import com.wen.pojo.RestResponse;
import com.wen.pojo.dto.QueryMediaParamsDto;
import com.wen.pojo.entity.MediaFiles;
import com.wen.pojo.entity.ResponseResult;
import com.wen.pojo.vo.UploadFilesVO;
import com.wen.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件上传接口
 */
@Slf4j
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {
    /**
     * 服务对象
     */
    @Autowired
    private MediaFilesService mediaFilesService;

    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public ResponseResult list(@RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        PageResult<MediaFiles> mediaFilesPageResult = mediaFilesService.queryMediaFiles(queryMediaParamsDto);
        return ResponseResult.okResult(mediaFilesPageResult);
    }

    /**
     * @param filedata 上传的文件，SpringMVC中将上传的文件封装到MultipartFile对象中，通过此对象可以获取文件相关信息
     */
    @ApiOperation("上传文件接口")
    @RequestMapping(value = "/upload/coursefile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseResult upload(@RequestPart("filedata") MultipartFile filedata) {
        if (filedata == null) {
            throw new IllegalArgumentException("上传文件为空");
        }
        MediaFiles mediaFiles = null;
        try {
            mediaFiles = mediaFilesService.uploadFile(filedata);
        } catch (Exception e) {
            ResponseResult.errorResult(400, "上传文件失败");
        }
        return ResponseResult.okResult(mediaFiles);
    }

    @ApiOperation(value = "获取媒体文件URL预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getUrlById(@PathVariable String mediaId) {
        try {
            MediaFiles mediaFile = mediaFilesService.getById(mediaId);
            if (mediaFile == null || StringUtils.isEmpty(mediaFile.getFileUrl())) {
                log.error("获取视频url失败，mediaId:{}", mediaId);
                throw new IllegalArgumentException("获取视频url失败");
            }
            return RestResponse.success(mediaFile.getFileUrl());
        } catch (Exception e) {
            log.error("获取视频url失败，mediaId:{}，错误信息:{}", mediaId, e.getMessage());
            throw new IllegalArgumentException("获取视频url失败", e);
        }
    }

}

