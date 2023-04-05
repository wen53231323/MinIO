package com.wen.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 媒资文件查询请求模型类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryMediaParamsDto {
    // 默认起始页码
    public static final long DEFAULT_PAGE_CURRENT = 1L;

    // 默认每页记录数
    public static final long DEFAULT_PAGE_SIZE = 10L;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Long pageNo = DEFAULT_PAGE_CURRENT;

    @ApiModelProperty(value = "每页记录数", example = "10")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    @ApiModelProperty("媒资文件名称")
    private String name;

    @ApiModelProperty("媒资类型")
    private String type;

    @ApiModelProperty("审核状态")
    private String status;
}
