package com.wen.service;

import io.minio.messages.Bucket;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author wen
 * @version 1.0
 * @description TODO 操作Minio模板接口
 * @date 2023/4/1 13:10
 */
public interface MinioTemplate {
    /**
     * 判断桶是否存在
     *
     * @param bucketName bucket名称
     * @return true存在，false不存在
     */
    Boolean bucketExists(String bucketName);

    /**
     * 创建桶（bucket）并设置权限公开访问
     *
     * @param bucketName bucket名称
     */
    void createBucket(String bucketName);

    /**
     * 创建桶并指定访问策略
     *
     * @param bucketName 桶名称
     * @param policyJson 访问策略JSON
     */
    void createBucket(String bucketName, String policyJson);

    /**
     * 获取所有的桶信息
     *
     * @return 所有桶信息
     */
    List<Bucket> getAllBuckets();

    /**
     * 获取所有桶名称列表
     */
    List<String> listBuckets();

    /**
     * 获取指定桶的访问策略
     *
     * @param bucketName 桶名称
     * @return 访问策略
     */
    String getBucketAccessPolicy(String bucketName);

    /**
     * 设置S3桶的公共访问策略或私有访问策略
     * 若访问策略为“public”，则将S3桶的访问策略设置为公共访问。
     * 若访问策略为“private”，则将S3桶的访问策略设置为私有访问，禁止所有用户进行读写操作。
     *
     * @param bucketName   桶名称
     * @param accessPolicy 访问策略（public或private）
     */
    void setBucketAccessPolicy(String bucketName, String accessPolicy);

    /**
     * 设置S3桶的自定义访问策略
     * 允许特定主体对桶内特定对象进行读取
     * 允许特定主体对桶内特定对象进行写入
     * 允许特定主体对桶内所有对象进行读取
     * 允许特定主体对桶内所有对象进行写入
     *
     * @param bucketName 桶名称
     * @param policyJson 自定义访问策略
     */
    void setBucketCustomAccessPolicys(String bucketName, String policyJson);

    /**
     * 设置桶的自定义访问策略
     * 允许特定主体对桶内特定对象进行读取
     * 允许特定主体对桶内特定对象进行写入
     * 允许特定主体对桶内所有对象进行读取
     * 允许特定主体对桶内所有对象进行写入
     *
     * @param bucketName 桶名称
     * @param actionList 允许的操作列表: 对象存储桶内对象获取和上传的操作列表;
     * @param principal  受信任主体: 对象存储桶授权的受信任主体
     */
    void setBucketCustomAccessPolicys(String bucketName, List<String> actionList, String principal);

    /**
     * 根据bucketName桶名称获取信息
     *
     * @param bucketName bucket名称
     * @return 单个桶信息
     */
    Optional<Bucket> getBucket(String bucketName);

    /**
     * 删除单个空桶，根据存储桶名称删除文件为空的桶（桶内无文件）
     *
     * @param bucketName bucket名称
     */
    void removeBucket(String bucketName);

    /**
     * 删除多个空桶，根据存储桶名称列表，依次删除多个文件为空的桶（桶内无文件）
     *
     * @param bucketNameList bucket名称列表
     */
    void removeBucketList(List<String> bucketNameList);

    /**
     * 根据objectName获取对应的MimeType
     * ContentInfoUtil是一个工具类，用于查询扩展名所对应的MimeType信息
     *
     * @param objectName 对象名称
     * @return MimeType或未知二进制流
     */
    String getContentType(String objectName);

    /**
     * 获取url,根据bucket和objectName生成对应的MinIO对象URL
     *
     * @param bucket     文件桶
     * @param objectName 对象名称
     * @return MinIO对象URL
     */
    String getUrl(String bucket, String objectName);

    /**
     * 上传本地文件到全局默认文件桶中
     *
     * @param filePath 本地文件路径
     * @return 文件对应的URL
     */
    String uploadFile(String filePath);

    /**
     * 上传本地文件到指定的桶
     *
     * @param filePath   本地文件路径
     * @param bucketName 文件桶
     * @return 文件对应的URL
     */
    String uploadFile(String filePath, String bucketName);

    /**
     * 上传本地文件到指定的桶下的文件夹中
     *
     * @param filePath   本地文件路径
     * @param bucketName 文件桶
     * @param objectName 对象名称
     * @return 文件对应的URL
     */
    String uploadFile(String filePath, String bucketName, String objectName);

    /**
     * 上传本地文件通用方法
     *
     * @param filePath    本地文件路径
     * @param bucketName  文件桶
     * @param objectName  对象名称
     * @param contentType 对象名称
     * @return 文件对应的URL
     */
    String putLocalFile(String filePath, String bucketName, String objectName, String contentType);

    /**
     * 上传File文件
     *
     * @param objectName  文件名
     * @param file        文件
     * @param contentType 文件类型
     * @return 文件对应的URL
     */
    String uploadFile(String objectName, File file, String contentType);

    /**
     * 上传File文件到指定的桶下
     *
     * @param bucketName  文件桶
     * @param objectName  文件名
     * @param file        文件
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     * @return 文件对应的URL
     */
    String uploadFile(String bucketName, String objectName, File file, String contentType);

    /**
     * 上传File文件通用方法
     *
     * @param bucketName  文件桶
     * @param objectName  文件名
     * @param file        文件
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     */
    void putFile(String bucketName, String objectName, File file, String contentType);

    /**
     * 上传MultipartFile文件通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param file       文件
     */
    void putMultipartFile(String bucketName, String objectName, MultipartFile file);

    /**
     * 上传MultipartFile文件到全局默认文件桶中
     *
     * @param file 文件
     * @return 文件对应的URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 上传MultipartFile文件到指定的文件夹下
     *
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param file       文件
     * @return 文件对应的URL
     */
    String uploadFile(String objectName, MultipartFile file);

    /**
     * 上传MultipartFile文件到指定的桶下的文件夹中
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param file       文件
     * @return 文件对应的URL
     */
    String uploadFile(String bucketName, String objectName, MultipartFile file);


    /**
     * 上传 bytes 通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param bytes      字节编码
     */
    void putBytes(String bucketName, String objectName, byte[] bytes, String contentType);

    /**
     * 上传bytes字节数组文件
     *
     * @param objectName  文件名
     * @param bytes       字节
     * @param contentType 文件类型
     * @return 文件对应的URL
     */
    String uploadFile(String objectName, byte[] bytes, String contentType);

    /**
     * 上传bytes字节数组到指定的文件桶下
     *
     * @param bucketName  桶名称
     * @param objectName  文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param bytes       字节
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     * @return 文件对应的URL
     */
    String uploadFile(String bucketName, String objectName, byte[] bytes, String contentType);

    /**
     * 上传InputStream流通用方法
     *
     * @param bucketName  桶名称
     * @param objectName  文件名
     * @param inputStream 文件流
     */
    void putInputStream(String bucketName, String objectName, InputStream inputStream, String contentType);

    /**
     * 上传InputStream流到对象存储服务中
     *
     * @param objectName  文件名（带有路径的完整文件名）
     * @param inputStream 文件流
     * @param contentType 文件类型
     * @return 文件对应的URL
     */
    String uploadFile(String objectName, InputStream inputStream, String contentType);

    /**
     * 上传InputStream流到指定的文件桶下
     *
     * @param bucketName  桶名称
     * @param objectName  文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param inputStream 文件流
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     * @return 文件对应的URL
     */
    String uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType);

    /**
     * 判断MinIO服务器上全局默认存储桶中，指定对象（文件）是否存在
     *
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @return true存在, 反之不存在
     */
    Boolean checkFileIsExist(String objectName);

    /**
     * 判断MinIO服务器上指定存储桶中，指定对象（文件）是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象（文件）名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @return 如果存在则返回true，否则返回false
     */
    Boolean checkFileIsExist(String bucketName, String objectName);

    /**
     * 下载文件，根据URL获取文件流
     *
     * @param url 文件的URL
     * @return 文件流
     */
    InputStream downloadFileStreamByUrl(String url);

    /**
     * 下载文件，根据文件全路径从全局默认桶获取文件流
     *
     * @param objectName 文件名称
     * @return 文件流
     */
    InputStream downloadFileStream(String objectName);

    /**
     * 下载文件，据文件桶和文件全路径获取文件流
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return 文件流
     */
    InputStream downloadFileStream(String bucketName, String objectName);

    /**
     * 下载文件，根据URL获取字节数组
     *
     * @param url 文件的URL
     * @return 字节数组
     */
    byte[] downloadFileByteByUrl(String url);

    /**
     * 下载文件，根据文件全路径从全局默认桶获取字节数组
     *
     * @param objectName 文件名称
     * @return 字节数组
     */
    byte[] downloadFileByte(String objectName);

    /**
     * 下载文件，据文件桶和根据文件全路径获取字节数组
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return 字节数组
     */
    byte[] downloadFileByte(String bucketName, String objectName);

    /**
     * 删除单个文件，删除全局默认桶中的文件
     *
     * @param objectName 文件名称
     * @return true成功、false失败
     */
    Boolean removeObject(String objectName);

    /**
     * 删除单个文件，删除指定桶中的文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     */
    Boolean removeObject(String bucketName, String objectName);

    /**
     * 删除多个文件，删除全局默认桶中的多个文件（列表中的所有文件）
     *
     * @param objectNameList 文件名称列表
     * @return true成功、false失败
     */
    Boolean removeObjectList(List<String> objectNameList);

    /**
     * 删除多个文件，删除指定桶中的多个文件（列表中的所有文件）
     *
     * @param bucketName     bucket名称
     * @param objectNameList 文件名称列表
     * @return true成功、false失败
     */
    Boolean removeObjectList(String bucketName, List<String> objectNameList);

    /**
     * 删除全局默认桶内的 文件夹及 文件夹内的所有文件
     *
     * @param directory 文件夹名称
     * @return true成功、false失败
     */
    Boolean removeAllObject(String directory);

    /**
     * 删除指定桶内的 文件夹及 文件夹内的所有文件
     *
     * @param bucketName bucket名称
     * @param directory  文件夹名称
     * @return true成功、false失败
     */
    Boolean removeAllObject(String bucketName, String directory);

    /**
     * 使用文件URL获取文件名
     *
     * @param url 文件URL
     * @return 文件名
     */
    String getObjectNameByUrl(String url);

    /**
     * 根据 URL 解析出全局默认桶文件目录
     *
     * @param url 需要解析的 URL
     * @return 文件目录
     */
    String getObjectPathByUrl(String url);

    /**
     * 根据 URL 解析出指定桶的文件目录
     *
     * @param bucketName 桶名称
     * @param url        需要解析的 URL
     * @return 文件目录
     */
    String getObjectPathByUrl(String bucketName, String url);

    /**
     * 从MinIO对象存储中下载分块文件，然后将其写入本地临时文件，并返回临时文件数组。
     * 注意使用完毕后要删除临时文件：for (File file : files) {file.delete();}
     *
     * @param bucketName      MinIO中的存储桶名称
     * @param chunkFileFolder 分块文件在MinIO存储桶中的路径
     * @param chunkTotal      分块文件的总数
     * @return 临时文件数组
     */
    File[] downloadChunkFile(String bucketName, String chunkFileFolder, int chunkTotal);

    /**
     * 清除指定路径下的所有分块文件
     *
     * @param bucketName 文件桶名称
     * @param path       分块文件的路径
     * @param chunkTotal 分块文件总数
     */
    void clearChunkFiles(String bucketName, String path, int chunkTotal);
}
