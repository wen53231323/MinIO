package com.wen.pojo.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.wen.pojo.enums.AppHttpCodeEnum;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

// @JsonInclude注解：为null的字段不序列化
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResult<T> implements Serializable {
    // 封装操作结果到flag中（状态码）
    @ApiModelProperty(value = "状态码")
    private Integer code;

    // 封装数据到data属性中（数据内容）
    @ApiModelProperty(value = "返回数据")
    private T data;

    // 封装特殊消息到message 属性msg中（状态信息）
    @ApiModelProperty(value = "状态信息")
    private String msg;

    // 构造器
    public ResponseResult() {
        this.code = AppHttpCodeEnum.SUCCESS.getCode();
        this.msg = AppHttpCodeEnum.SUCCESS.getMsg();
    }

    // 构造器
    public ResponseResult(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    // 构造器
    public ResponseResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 构造器
    public ResponseResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // 响应成功方法：返回空参
    public static ResponseResult okResult() {
        ResponseResult result = new ResponseResult();
        return result;
    }

    // 响应成功方法：返回状态码、状态信息（状态码、状态信息来自枚举）
    public static ResponseResult okResult(int code, String msg) {
        ResponseResult result = new ResponseResult();
        return result.ok(code, null, msg);
    }

    // 响应成功方法：返回数据内容、状态码、状态信息（状态码、状态信息来自枚举）
    public static ResponseResult okResult(Object data) {
        ResponseResult result = setAppHttpCodeEnum(AppHttpCodeEnum.SUCCESS, AppHttpCodeEnum.SUCCESS.getMsg());
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    // 响应失败方法：返回状态码、状态信息（状态码、状态信息来自枚举）
    public static ResponseResult errorResult(int code, String msg) {
        ResponseResult result = new ResponseResult();
        return result.error(code, msg);
    }

    // 响应失败方法：返回状态码、状态信息（状态码、状态信息来自枚举）
    public static ResponseResult errorResult(AppHttpCodeEnum enums) {
        return setAppHttpCodeEnum(enums, enums.getMsg());
    }

    // 响应失败方法：返回状态码、状态信息（状态码、状态信息来自枚举）
    public static ResponseResult errorResult(AppHttpCodeEnum enums, String msg) {
        return setAppHttpCodeEnum(enums, msg);
    }

    public static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums) {
        return okResult(enums.getCode(), enums.getMsg());
    }

    private static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums, String msg) {
        return okResult(enums.getCode(), msg);
    }

    // 响应失败方法：返回状态码、状态信息（状态码、状态信息来自枚举）
    public ResponseResult<?> error(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

    // 响应成功方法：返回数据内容、状态码（状态码来自枚举）
    public ResponseResult<?> ok(Integer code, T data) {
        this.code = code;
        this.data = data;
        return this;
    }

    // 响应成功方法：返回数据内容、状态码、状态信息（状态码、状态信息来自枚举）
    public ResponseResult<?> ok(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        return this;
    }

    // 响应成功方法：返回数据内容
    public ResponseResult<?> ok(T data) {
        this.data = data;
        return this;
    }

    // get方法：获取状态码
    public Integer getCode() {
        return code;
    }

    // set方法：设置状态码
    public void setCode(Integer code) {
        this.code = code;
    }

    // get方法：获取状态信息
    public String getMsg() {
        return msg;
    }

    // set方法：设置状态信息
    public void setMsg(String msg) {
        this.msg = msg;
    }

    // get方法：获取数据内容
    public T getData() {
        return data;
    }

    // set方法：设置数据内容
    public void setData(T data) {
        this.data = data;
    }

}