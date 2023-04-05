package com.wen.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 媒资文件表(MediaFiles)表实体类
 *
 * @author wen
 * @since 2023-04-04 14:56:51
 */
@SuppressWarnings("serial") // jse提供的注解，屏蔽无关紧要的警告。
@Data // Lombok提供的注解，代表get、set、toString、equals、hashCode等操作
@Accessors(chain=true) // Lombok提供的注解，代表对应字段的 setter 方法调用后，会返回当前对象，代替返回的void
@NoArgsConstructor // Lombok提供的注解，代表无参构造
@AllArgsConstructor // Lombok提供的注解，代表全参构造
@TableName("media_files") // Mybatis-plus提供的注解，用于标识实体类对应的表名
@ApiModel(value="MediaFiles",description="媒资文件表") // Swagger2提供的注解，value设置类名，description设置描述信息
public class MediaFiles implements Serializable{ // 实现Serializable接口，对象能被序列化
    
    private static final long serialVersionUID = -77498866580227286L; // 反序列化的过程需要使用serialVersionUID

    @ApiModelProperty("文件ID") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private Long fileId;

    @ApiModelProperty("上传人ID") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private Long userId;

    @ApiModelProperty("上传人姓名") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String userName;

    @ApiModelProperty("文件content_type") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String contentType;

    @ApiModelProperty("文件名称") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileName;

    @ApiModelProperty("文件大小，单位为字节") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private Long fileSize;

    @ApiModelProperty("文件类型（图片、文档、视频、音乐、其他）") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileType;

    @ApiModelProperty("存储在文件系统的桶名") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileBucket;

    @ApiModelProperty("存储在文件系统的路径") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String filePath;

    @ApiModelProperty("媒资文件访问完整地址") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileUrl;

    @ApiModelProperty("文件的 MD5 哈希值，用于分块上传大文件") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileMd5;

    @ApiModelProperty("文件标识，用于快速搜索和分类文件") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileTag;

    @ApiModelProperty("文件备注") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String fileRemark;

    @ApiModelProperty("状态（0表示非正常，1表示正常）") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private Integer fileStatus;

    @ApiModelProperty("是否展示（0表示不展示，1表示展示）") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private Integer isDisplay;

    @ApiModelProperty("审核状态（未审核、审核中、审核通过、审核不通过）") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String auditStatus;

    @ApiModelProperty("审核意见") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private String auditMind;

    @ApiModelProperty("文件上传时间，格式为 yyyy-MM-dd HH:mm:ss") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private LocalDateTime createTime;

    @ApiModelProperty("文件修改时间，格式为 yyyy-MM-dd HH:mm:ss") // Swagger2提供的注解，value设置描述信息，required设置参数是否必填
    private LocalDateTime updateTime;
}

