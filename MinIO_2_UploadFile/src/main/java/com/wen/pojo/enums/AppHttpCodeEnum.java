package com.wen.pojo.enums;

public enum AppHttpCodeEnum {
    // 枚举类对象必须在第一行声明，多个枚举类对象之间用","隔开，用";"结束
    SUCCESS(200, "操作成功"),
    NEED_LOGIN(401, "需要登录后操作"),
    NO_OPERATOR_AUTH(403, "无权限操作"),
    SYSTEM_ERROR(500, "出现错误"),
    ACCOUNT_EXIST(501, "账号已存在"),
    PHONENUMBER_EXIST(502, "手机号已存在"),
    EMAIL_EXIST(503, "邮箱已存在"),
    REQUIRE_USERNAME(504, "必需填写账号"),
    LOGIN_ERROR(505, "用户名或密码错误"),
    CONTENT_NOT_NULL(506, "评论内容不能为空"),
    FILE_TYPE_ERROR(507, "文件类型错误，仅支持png、jpg、jpeg、gif、bmp"),
    USERACCOUNT_NOT_NULL(508,"用户账号不能为空"),
    PASSWORD_NOT_NULL(509,"用户密码不能为空"),
    EMAIL_NOT_NULL(510,"用户邮箱不能为空"),
    NICKNAME_NOT_NULL(510,"用户昵称不能为空");
    // 状态码
    int code;
    // 错误信息
    String msg;

    // 私有化的构造器，并给对象属性初始化（枚举类的构造器只能使用 private 权限修饰符）
    AppHttpCodeEnum(int code, String errorMessage) {
        this.code = code;
        this.msg = errorMessage;
    }

    // 只需要添加get方法就行
    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
