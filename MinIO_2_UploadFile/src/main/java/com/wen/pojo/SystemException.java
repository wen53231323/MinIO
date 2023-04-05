package com.wen.pojo;


import com.wen.pojo.enums.AppHttpCodeEnum;

/**
 * @author wen
 * @version 1.0
 * @description TODO 自定义异常处理类
 * @date 2023/4/3 17:00
 * （1）继承于现有的异常结构：RuntimeException 、Exception
 * （2）提供全局常量：serialVersionUID
 * （3）提供重载的构造器
 */
public class SystemException extends RuntimeException {

    private int code;

    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 构造方法
     *
     * @param httpCodeEnum AppHttpCodeEnum为自定义枚举类，封装 状态码 与 返回信息
     */
    public SystemException(AppHttpCodeEnum httpCodeEnum) {
        super(httpCodeEnum.getMsg());
        this.code = httpCodeEnum.getCode();
        this.msg = httpCodeEnum.getMsg();
    }

    public SystemException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

