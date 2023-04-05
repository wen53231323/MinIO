package com.wen.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wen
 * @version 1.0
 * @description TODO
 * @date 2023/3/12 10:22
 */
@Data
public class UploadFilesVO {

    @ApiModelProperty("文件名称")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileName;

    @ApiModelProperty("资源的媒体类型 mimeType")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String contentType;

    @ApiModelProperty("文件类型（图片,文档,视频,音乐,其他）")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileType;

    @ApiModelProperty("文件大小")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private Long fileSize;

    @ApiModelProperty("标签")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileTags;

    @ApiModelProperty("用户名")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String userName;

    @ApiModelProperty("备注")// Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String remark;

}
