package com.wen.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.wen.config.MinioAutoProperties;
import com.wen.pojo.RestResponse;
import com.wen.pojo.SystemException;
import com.wen.service.MinioTemplate;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wen
 * @version 1.0
 * @description TODO Minio对象存储操作模板接口实现类
 * @date 2023/4/1 13:43
 */
@Slf4j
@Service
public class MinioTemplateImpl implements MinioTemplate {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioAutoProperties minioAutoProperties;

    /**
     * 判断桶是否存在
     *
     * @param bucketName bucket名称
     * @return true存在，false不存在
     */
    @Override
    public Boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("检查桶是否存在失败：{}", e.getMessage());
            throw new RuntimeException("检查桶是否存在失败!", e);
        }
    }

    /**
     * 创建桶（bucket）访问策略为全部可读可写
     *
     * @param bucketName bucket名称
     */
    @Override
    public void createBucket(String bucketName) {
        if (!this.bucketExists(bucketName)) {
            try {
                // 创建桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .objectLock(false)// 不启用对象锁
                                .build()
                );
                // 设置桶的访问策略为允许匿名访问 S3 桶中的所有对象
                String policyJson = "{\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Action\": [ \"s3:GetObject\", \"s3:PutObject\", \"s3:DeleteObject\", \"s3:ListBucket\" ],\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": { \"AWS\": [ \"*\" ] },\n" +
                        "            \"Resource\": [ \"arn:aws:s3:::" + bucketName + "/*\", \"arn:aws:s3:::" + bucketName + "\" ],\n" +
                        "            \"Sid\": \"PublicReadWrite\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"Version\": \"2012-10-17\"\n" +
                        "}";
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policyJson)
                                .build()
                );
            } catch (Exception e) {
                log.error("创建桶失败：{}", e.getMessage());
                throw new RuntimeException("创建桶失败!", e);
            }
        }
    }

    /**
     * 创建桶并指定访问策略
     *
     * @param bucketName 桶名称
     * @param policyJson 访问策略JSON
     */
    @Override
    public void createBucket(String bucketName, String policyJson) {
        // 检查桶是否存在
        if (!this.bucketExists(bucketName)) {
            try {
                // 创建桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .objectLock(false)// 不启用对象锁
                                .build()
                );
                // 设置桶策略
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policyJson)// 设置访问策略JSON
                                .build()
                );
            } catch (Exception e) {
                log.error("创建桶失败：{}", e.getMessage());
                throw new RuntimeException("创建桶失败!", e);
            }
        }
    }

    /**
     * 上传MultipartFile通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param file       文件
     */
    @Override
    public void putMultipartFile(String bucketName, String objectName, MultipartFile file) {
        // 校验参数是否为空
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("桶名称不能为空");
        }
        if (objectName == null || objectName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 如果 filedata.getContentType() 返回值为 null，则返回默认值 MediaType.APPLICATION_OCTET_STREAM_VALUE 未知的二进制流
        String contentType = Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        // 获取文件输入流，并使用try-with-resources自动关闭流
        try (InputStream inputStream = file.getInputStream()) {
            // 构建PutObjectArgs对象，上传数据流至MinIO服务器
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)// 设置存储桶名称
                            .object(objectName)// 设置文件对象名称
                            .contentType(contentType)// 设置文件类型
                            .stream(inputStream, inputStream.available(), -1)// 数据流，可读取的字节数，为-1时表示读取到流的末尾
                            .build()
            );
        } catch (IOException e) {// 捕获文件输入流获取异常
            log.error("文件流获取错误：{}", e.getMessage());
            throw new RuntimeException("文件流获取错误:", e);
        } catch (Exception e) {// 捕获其他异常，如文件上传异常
            log.error("文件上传错误：{}", e.getMessage());
            throw new RuntimeException("文件上传错误", e);
        }
    }

    /**
     * 上传MultipartFile文件到全局默认文件桶中
     *
     * @param file 文件
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(MultipartFile file) {
        // 给文件名添加时间戳防止重复
        String fileName = getUniqueFilePath(Objects.requireNonNull(file.getOriginalFilename()));
        // 开始上传
        this.putMultipartFile(minioAutoProperties.getBucket(), fileName, file);
        return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + fileName;
    }

    /**
     * 上传InputStream通用方法
     *
     * @param bucketName  桶名称
     * @param objectName  文件名
     * @param inputStream 文件流
     */
    @Override
    public void putInputStream(String bucketName, String objectName, InputStream inputStream, String contentType) {
        try {
            // 构建PutObjectArgs对象，将数据流上传至MinIO服务器
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)// 设置存储桶名称
                            .object(objectName)// 设置文件对象名称
                            .contentType(contentType)// 设置文件内容类型
                            .stream(inputStream, inputStream.available(), -1)// 数据流，可读取的字节数，为-1时表示读取到流的末尾
                            .build()
            );
        } catch (Exception e) {
            log.error("文件流上传错误：{}", e.getMessage());
            throw new RuntimeException("文件流上传错误", e);
        }
    }

    /**
     * 上传InputStream流到对象存储服务中
     *
     * @param objectName  文件名（带有路径的完整文件名）
     * @param inputStream 文件流
     * @param contentType 文件类型
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String objectName, InputStream inputStream, String contentType) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);

        // 开始上传
        this.putInputStream(minioAutoProperties.getBucket(), objectName, inputStream, contentType);
        return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
    }

    /**
     * 上传 bytes 通用方法
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param bytes      字节编码
     */
    @Override
    public void putBytes(String bucketName, String objectName, byte[] bytes, String contentType) {
        // 字节转文件流
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            // 使用MinIOClient的putObject方法上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)// 设置存储桶名称
                            .object(objectName)// 设置文件对象名称
                            .contentType(contentType)// 设置文件内容类型
                            .stream(inputStream, inputStream.available(), -1)// 数据流，可读取的字节数，为-1时表示读取到流的末尾
                            .build()
            );
        } catch (Exception e) {
            log.error("文件流上传错误", e);
            throw new RuntimeException("文件流上传错误", e);
        }
    }

    /**
     * 上传bytes字节数组文件
     *
     * @param objectName  文件名
     * @param bytes       字节
     * @param contentType 文件类型
     * @return 文件url
     */
    @Override
    public String uploadFile(String objectName, byte[] bytes, String contentType) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);
        // 开始上传
        this.putBytes(minioAutoProperties.getBucket(), objectName, bytes, contentType);
        return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
    }

    /**
     * 上传MultipartFile文件到指定的文件夹下
     *
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param file       文件
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String objectName, MultipartFile file) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);
        // 开始上传
        this.putMultipartFile(minioAutoProperties.getBucket(), objectName, file);
        return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
    }

    /**
     * 上传MultipartFile文件到指定的桶下的文件夹中
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param file       文件
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);

        // 判断桶名称是否为空，如果为空默认上传默认桶
        if (StringUtils.isEmpty(bucketName)) {
            this.uploadFile(objectName, file);
            return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
        }

        // 不为空则判断是否存在，不存在则创建
        if (!bucketExists(bucketName)) {
            log.info("Bucket桶" + bucketName + "不存在，开始创建...");
            this.createBucket(bucketName);
            log.info("Bucket桶" + bucketName + "创建成功！");
        }

        // 开始上传
        this.putMultipartFile(bucketName, objectName, file);

        // 响应url
        return minioAutoProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
    }


    /**
     * 上传InputStream流到指定的文件桶下
     *
     * @param bucketName  桶名称
     * @param objectName  文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param inputStream 文件流
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);

        // 判断桶名称是否为空，如果为空默认上传默认桶
        if (StringUtils.isEmpty(bucketName)) {
            this.uploadFile(objectName, inputStream, contentType);
            return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
        }

        // 不为空则判断是否存在，不存在则创建
        if (!bucketExists(bucketName)) {
            log.info("Bucket桶" + bucketName + "不存在，开始创建...");
            this.createBucket(bucketName);
            log.info("Bucket桶" + bucketName + "创建成功！");
        }

        // 开始上传
        this.putInputStream(bucketName, objectName, inputStream, contentType);
        return minioAutoProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
    }

    /**
     * 上传bytes字节数组到指定的文件桶下
     *
     * @param bucketName  桶名称
     * @param objectName  文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @param bytes       字节
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String bucketName, String objectName, byte[] bytes, String contentType) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);

        // 判断桶名称是否为空，如果为空默认上传默认桶
        if (StringUtils.isEmpty(bucketName)) {
            this.uploadFile(objectName, bytes, contentType);
            return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
        }

        // 桶名称不为空则判断是否存在，不存在则创建
        if (!bucketExists(bucketName)) {
            log.info("Bucket桶" + bucketName + "不存在，开始创建...");
            this.createBucket(bucketName);
            log.info("Bucket桶" + bucketName + "创建成功！");
        }

        // 开始上传
        this.putBytes(bucketName, objectName, bytes, contentType);
        return minioAutoProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
    }

    /**
     * 上传文件到MinIO服务器
     *
     * @param bucketName  存储桶名称
     * @param objectName  文件对象名称，即存储到服务器上的路径和文件名
     * @param file        待上传的文件
     * @param contentType 文件内容类型，示例：application/json、image/png、text/plain等
     */
    @Override
    public void putFile(String bucketName, String objectName, File file, String contentType) {
        // 创建文件输入流
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // 使用MinIOClient的putObject方法上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName) // 设置存储桶名称
                            .object(objectName) // 设置文件对象名称
                            .contentType(contentType) // 设置文件内容类型
                            .stream(fileInputStream, fileInputStream.available(), -1) // 数据流，可读取的字节数，为-1时表示读取到流的末尾
                            .build()
            );
        } catch (Exception e) {
            log.error("文件上传错误", e);
            throw new RuntimeException("文件上传错误", e);
        }
    }

    /**
     * 上传File文件
     *
     * @param objectName  文件名
     * @param file        文件
     * @param contentType 文件类型
     * @return 文件url
     */
    @Override
    public String uploadFile(String objectName, File file, String contentType) {
        // 给文件名添加时间戳防止重复
        objectName = getUniqueFilePath(objectName);

        // 上传文件到默认桶
        putFile(minioAutoProperties.getBucket(), objectName, file, contentType);

        // 响应文件URL
        return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + objectName;
    }

    /**
     * 上传File文件到指定的桶下
     *
     * @param bucketName  文件桶
     * @param objectName  文件名
     * @param file        文件
     * @param contentType 文件类型, 例如 image/jpeg: jpg图片格式, 详细可看: https://www.runoob.com/http/http-content-type.html
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String bucketName, String objectName, File file, String contentType) {
        // 给文件名添加时间戳防止重复
        String uniqueFilePath = getUniqueFilePath(objectName);

        // 判断桶名称是否为空，如果为空默认上传默认桶
        if (StringUtils.isEmpty(bucketName)) {
            this.uploadFile(objectName, file, contentType);
            return minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/" + uniqueFilePath;
        }

        // 桶名称不为空则判断是否存在，不存在则创建
        if (!bucketExists(bucketName)) {
            log.info("Bucket桶" + bucketName + "不存在，开始创建...");
            this.createBucket(bucketName);
            log.info("Bucket桶" + bucketName + "创建成功！");
        }

        // 开始上传
        this.putFile(bucketName, uniqueFilePath, file, contentType);
        // 响应url
        return minioAutoProperties.getEndpoint() + "/" + bucketName + "/" + uniqueFilePath;
    }


    /**
     * 判断文件是否存在
     *
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @return true存在, 反之不存在
     */
    @Override
    public Boolean checkFileIsExist(String objectName) {
        return this.checkFileIsExist(minioAutoProperties.getBucket(), objectName);
    }

    /**
     * 判断MinIO服务器上指定存储桶中，指定对象（文件）是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象（文件）名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @return 如果存在则返回true，否则返回false
     */
    @Override
    public Boolean checkFileIsExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            // 调用statObject方法查询指定对象的信息，如果该对象不存在，则会抛出异常
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            if (statObjectResponse.size() == 0) {
                exist = false;
            }
        } catch (Exception e) {
            // 查询过程中发生异常，则说明指定对象不存在，返回false
            exist = false;
        }
        return exist;
    }


    /**
     * 下载文件，根据URL获取文件流
     *
     * @param url 文件的URL
     * @return 文件流
     */
    @Override
    public InputStream downloadFileStreamByUrl(String url) {
        try {
            return new URL(url).openStream();
        } catch (IOException e) {
            log.error("根据URL获取流失败：{}", e.getMessage());
            throw new RuntimeException("根据URL获取流失败!", e);
        }
    }

    /**
     * 下载文件，根据文件全路径获取文件流
     *
     * @param objectName 文件名称
     * @return 文件流
     */
    @Override
    public InputStream downloadFileStream(String objectName) {
        return this.downloadFileStream(minioAutoProperties.getBucket(), objectName);
    }

    /**
     * 下载文件，据文件桶和文件全路径获取文件流
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return 文件流
     */
    @Override
    public InputStream downloadFileStream(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("根据文件名获取流失败：{}", e.getMessage());
            throw new RuntimeException("根据文件名获取流失败!", e);
        }
    }

    /**
     * 根据URL下载文件，返回字节数组
     *
     * @param url 文件的URL
     * @return 字节数组
     */
    @Override
    public byte[] downloadFileByteByUrl(String url) {
        try {
            // 将URL字符串转换为URL对象
            URL fileUrl = new URL(url);
            // 定义字节数组输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 打开文件输入流
            InputStream inputStream = fileUrl.openStream();
            // 定义读取缓存和读取长度
            byte[] buffer = new byte[1024];
            int length;

            // 循环读取文件数据到字节数组输出流中
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // 关闭输入流
            inputStream.close();
            // 返回字节数组
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("根据URL下载文件失败：{}", e.getMessage());
            throw new RuntimeException("根据URL下载文件失败！", e);
        }
    }

    /**
     * 根据Minio框架自带配置文件中的桶名和文件名下载文件，返回字节数组
     *
     * @param objectName 文件名称
     * @return 字节数组
     */
    @Override
    public byte[] downloadFileByte(String objectName) {
        // 调用另一个downloadFileByte方法，并使用桶名参数为Minio框架自带配置文件中的桶名
        return this.downloadFileByte(minioAutoProperties.getBucket(), objectName);
    }

    /**
     * 根据桶名和文件名下载文件，返回字节数组
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @return 字节数组
     */
    @Override
    public byte[] downloadFileByte(String bucketName, String objectName) {
        try {
            // 定义字节数组输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 打开文件输入流
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
            // 定义读取缓存和读取长度
            byte[] buffer = new byte[1024];
            int length;

            // 循环读取文件数据到字节数组输出流中
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // 关闭输入流
            inputStream.close();
            // 返回字节数组
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("根据文件名下载文件失败：{}", e.getMessage());
            throw new RuntimeException("根据文件名下载文件失败！", e);
        }
    }

    /**
     * 获取所有的桶信息
     *
     * @return 所有桶信息
     */
    @Override
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取全部存储桶失败：{}", e.getMessage());
            throw new RuntimeException("获取全部存储桶失败!", e);
        }
    }

    /**
     * 获取所有桶名称列表
     */
    @Override
    public List<String> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<String> bucketNames = new ArrayList<>();
            for (Bucket bucket : buckets) {
                bucketNames.add(bucket.name());
            }
            return bucketNames;
        } catch (Exception e) {
            log.error("获取所有桶名称失败：{}", e.getMessage());
            throw new RuntimeException("获取所有桶名称失败!", e);
        }
    }

    /**
     * 获取指定桶的访问策略
     *
     * @param bucketName 桶名称
     * @return 访问策略
     */
    @Override
    public String getBucketAccessPolicy(String bucketName) {
        try {
            String policy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
            return policy;
        } catch (Exception e) {
            log.error("获取指定桶的访问策略失败：{}", e.getMessage());
            throw new RuntimeException("获取指定桶的访问策略失败!", e);
        }
    }

    /**
     * 设置S3桶的公共访问策略或私有访问策略
     * 若访问策略为“public”，则将S3桶的访问策略设置为公共访问。
     * 若访问策略为“private”，则将S3桶的访问策略设置为私有访问，禁止所有用户进行读写操作。
     *
     * @param bucketName   桶名称
     * @param accessPolicy 访问策略（public或private）
     */
    @Override
    public void setBucketAccessPolicy(String bucketName, String accessPolicy) {
        // 检查桶是否存在
        if (!bucketExists(bucketName)) {
            log.error("Bucket桶{}不存在", bucketName);
            throw new RuntimeException("Bucket桶" + bucketName + "不存在！");
        }
        try {
            // 如果访问策略为公共访问
            if ("public".equalsIgnoreCase(accessPolicy)) {
                String policyJson = "{\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Action\": [ \"s3:GetObject\", \"s3:PutObject\", \"s3:DeleteObject\", \"s3:ListBucket\" ],\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": { \"AWS\": [ \"*\" ] },\n" +
                        "            \"Resource\": [ \"arn:aws:s3:::" + bucketName + "/*\", \"arn:aws:s3:::" + bucketName + "\" ],\n" +
                        "            \"Sid\": \"PublicReadWrite\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"Version\": \"2012-10-17\"\n" +
                        "}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
            }
            // 如果访问策略为私有
            else if ("private".equalsIgnoreCase(accessPolicy)) {
                String policyText = "{\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Deny\",\n" +
                        "            \"Principal\": \"*\",\n" +
                        "            \"Action\": \"s3:GetObject\",\n" +
                        "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"Effect\": \"Deny\",\n" +
                        "            \"Principal\": \"*\",\n" +
                        "            \"Action\": \"s3:PutObject\",\n" +
                        "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"Version\": \"2012-10-17\"\n" +
                        "}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policyText).build());
            }
        } catch (Exception e) {
            log.error("设置桶访问策略失败：{}", e.getMessage());
            throw new RuntimeException("设置桶访问策略失败!", e);
        }
    }

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
    @Override
    public void setBucketCustomAccessPolicys(String bucketName, String policyJson) {
        try {
            // 检查桶是否存在
            if (!bucketExists(bucketName)) {
                log.error("Bucket桶{}不存在", bucketName);
                throw new RuntimeException("Bucket桶" + bucketName + "不存在！");
            }
            // 设置桶的访问策略
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policyJson)
                            .build()
            );
        } catch (Exception e) {
            log.error("设置桶访问策略失败：{}", e.getMessage());
            throw new RuntimeException("设置桶访问策略失败!", e);
        }
    }

    @Override
    public void setBucketCustomAccessPolicys(String bucketName, List<String> actionList, String principal) {
        try {
            // 判断 Bucket 是否存在
            if (!bucketExists(bucketName)) {
                String errorMessage = String.format("Bucket桶 %s 不存在", bucketName);
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            // 设置操作列表
            String actionsStr = "";
            if (actionList != null && !actionList.isEmpty()) {
                actionsStr = "\"Action\": [" + actionList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "],\n";
            }

            // 设置授权主体
            String principalStr = "\"Principal\": { \"AWS\": [\"" + (principal != null && !principal.isEmpty() ? principal : "*") + "\"] },\n";

            // 拼接访问策略
            String policyString = new StringBuilder()
                    .append("{\n")
                    .append("    \"Statement\": [\n")
                    .append("        {\n")
                    .append(actionsStr)
                    .append("            \"Effect\": \"Allow\",\n")
                    .append(principalStr)
                    .append("            \"Resource\": [\"arn:aws:s3:::")
                    .append(bucketName)
                    .append("/*\"]\n")
                    .append("        }\n")
                    .append("    ],\n")
                    .append("    \"Version\": \"2012-10-17\"\n")
                    .append("}")
                    .toString();

            // 设置 Bucket 访问策略
            SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder().bucket(bucketName).config(policyString).build();
            minioClient.setBucketPolicy(setBucketPolicyArgs);

        } catch (Exception e) {
            log.error("设置指定桶的访问策略失败，MinIO异常:{}", e.getMessage());
            throw new RuntimeException("设置指定桶的访问策略失败，MinIO异常!", e);
        }
    }

    /**
     * 据bucketName桶名称获取信息
     *
     * @param bucketName bucket名称
     * @return
     */
    @Override
    public Optional<Bucket> getBucket(String bucketName) {
        try {
            return minioClient.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
        } catch (Exception e) {
            log.error("根据存储桶名称获取信息失败：{}", e.getMessage());
            throw new RuntimeException("根据存储桶名称获取信息失败!", e);
        }
    }

    /**
     * 删除单个空桶，根据存储桶名称删除文件为空的桶（桶内无文件）
     *
     * @param bucketName bucket名称
     */
    @Override
    public void removeBucket(String bucketName) {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (bucketExists) {
                Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
                // 判断存储桶是否存在对象
                if (objects.iterator().hasNext()) {
                    log.warn("存储桶 {} 不为空，无法删除", bucketName);
                } else {
                    minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
                    log.info("存储桶 {} 删除成功", bucketName);
                }
            } else {
                log.warn("存储桶 {} 不存在，无法删除", bucketName);
            }
        } catch (Exception e) {
            log.error("根据存储桶名称删除桶失败：{}", e.getMessage());
            throw new RuntimeException("根据存储桶名称删除桶失败!", e);
        }
    }

    /**
     * 删除多个空桶，根据存储桶名称列表，依次删除多个文件为空的桶（桶内无文件）
     *
     * @param bucketNameList bucket名称列表
     */
    @Override
    public void removeBucketList(List<String> bucketNameList) {
        if (bucketNameList == null || bucketNameList.isEmpty()) {
            log.info("待删除的存储桶名称列表不能为空");
            throw new IllegalArgumentException("待删除的存储桶名称列表不能为空");
        }
        for (String bucketName : bucketNameList) {
            try {
                boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (bucketExists) {
                    Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
                    // 判断存储桶是否存在对象
                    if (objects.iterator().hasNext()) {
                        log.warn("存储桶 {} 不为空，无法删除", bucketName);
                    } else {
                        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
                        log.info("存储桶 {} 删除成功", bucketName);
                    }
                } else {
                    log.warn("存储桶 {} 不存在，无法删除", bucketName);
                }
            } catch (Exception e) {
                log.error("删除存储桶 {} 失败：{}", bucketName, e.getMessage());
                throw new RuntimeException("根据存储桶名称删除桶失败!", e);
            }
        }
    }

    @Override
    public String uploadFile(String filePath) {
        // 获取文件的MIME类型
        File file = new File(filePath);
        String contentType = getContentType(file.getName());
        try {
            String url = this.putLocalFile(filePath, minioAutoProperties.getBucket(), file.getName(), contentType);
            return url;
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new SystemException(400, "上传到文件系统出错:" + e.getMessage());
        }
    }

    @Override
    public String uploadFile(String filePath, String bucketName) {
        // 获取文件的MIME类型
        File file = new File(filePath);
        String contentType = getContentType(file.getName());
        try {
            String url = this.putLocalFile(filePath, bucketName, file.getName(), contentType);
            return url;
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new SystemException(400, "上传到文件系统出错:" + e.getMessage());
        }
    }

    /**
     * 上传本地文件
     *
     * @param filePath   本地文件路径
     * @param bucketName 文件桶
     * @param objectName 对象名称
     * @return 文件对应的URL
     */
    @Override
    public String uploadFile(String filePath, String bucketName, String objectName) {
        // 根据objectName获取对应的MimeType
        String contentType = getContentType(objectName);
        try {
            String url = this.putLocalFile(filePath, bucketName, objectName, contentType);
            return url;
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new SystemException(400, "上传到文件系统出错:" + e.getMessage());
        }
    }

    /**
     * 上传本地文件通用方法
     *
     * @param filePath    本地文件路径
     * @param bucketName  文件桶
     * @param objectName  对象名称
     * @param contentType 对象名称
     * @return 文件对应的URL
     */
    @Override
    public String putLocalFile(String filePath, String bucketName, String objectName, String contentType) {
        Objects.requireNonNull(filePath, "本地文件路径不能为空");
        Objects.requireNonNull(bucketName, "桶不能为空");
        Objects.requireNonNull(objectName, "对象名称不能为空");
        // 根据传入的文件名生成一个唯一的文件名
        objectName = getUniqueFilePath(objectName);
        try {
            minioClient.uploadObject(
                    UploadObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(filePath)
                            .contentType(contentType)
                            .build()
            );
            return minioAutoProperties.getEndpoint() + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new SystemException(400, "上传到文件系统出错:" + e.getMessage());
        }
    }

    /**
     * 根据objectName获取对应的MimeType
     * ContentInfoUtil是一个工具类，用于查询扩展名所对应的MimeType信息
     *
     * @param objectName 对象名称
     * @return MimeType或未知二进制流
     */
    @Override
    public String getContentType(String objectName) {
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(objectName)) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        // 默认content-type为未知二进制流
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // 判断对象名是否包含 .
        int dotIndex = objectName.lastIndexOf(".");
        if (dotIndex > 0) {
            // 有 .  则划分出扩展名
            String extension = objectName.substring(dotIndex);
            // 根据扩展名得到content-type，如果为未知扩展名，例如 .abc之类的东西，则会返回null
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            // 如果得到了正常的content-type，则重新赋值，覆盖默认类型
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

    /**
     * 根据bucket和objectName生成对应的MinIO对象URL
     *
     * @param bucket     文件桶
     * @param objectName 对象名称
     * @return MinIO对象URL
     */
    @Override
    public String getUrl(String bucket, String objectName) {
        String url = "";
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("生成MinIO对象URL失败，bucket: {}, objectName: {}, 错误信息：{}", bucket, objectName, e.getMessage());
        }
        return url;
    }


    /**
     * 删除文件
     *
     * @param objectName 文件名称
     */
    @Override
    public Boolean removeObject(String objectName) {
        try {
            this.removeObject(minioAutoProperties.getBucket(), objectName);
            log.info("文件 {} 删除成功", objectName);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     */
    @Override
    public Boolean removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return true;
        } catch (Exception e) {
            log.error("删除文件失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean removeObjectList(List<String> objectNameList) {
        try {
            this.removeObjectList(minioAutoProperties.getBucket(), objectNameList);
            log.info("文件列表 {} 删除成功", objectNameList);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean removeObjectList(String bucketName, List<String> objectNameList) {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            for (String objectName : objectNameList) {
                objects.add(new DeleteObject(objectName));
            }
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build());
            for (Result<DeleteError> result : results) {
                result.get(); // 触发异常抛出以便捕获
            }
            return true;
        } catch (Exception e) {
            log.error("删除文件失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean removeAllObject(String directory) {
        try {
            this.removeAllObject(minioAutoProperties.getBucket(), directory);
            log.info("文件列表 {} 删除成功", directory);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除指定桶内的 文件夹及 文件夹内的所有文件
     *
     * @param bucketName bucket名称
     * @param directory  文件夹名称
     * @return true成功、false失败
     */
    @Override
    public Boolean removeAllObject(String bucketName, String directory) {
        try {
            // 列出当前文件夹的所有文件和子文件夹
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(directory)
                            .recursive(true)
                            .build());
            List<DeleteObject> objects = new LinkedList<>();
            for (Result<Item> result : results) {
                // 逐个将文件名添加到待删除对象列表中
                objects.add(new DeleteObject(result.get().objectName()));
            }
            // 调用Minio SDK的removeObjects方法执行删除操作，并获取操作结果
            Iterable<Result<DeleteError>> removeResults = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            // 遍历删除操作的结果
            for (Result<DeleteError> result : removeResults) {
                // 获取删除操作的错误信息
                DeleteError error = null;
                try {
                    error = result.get();
                } catch (Exception e) {
                    // 如果获取失败，则记录错误日志
                    log.error("删除文件失败：{}", e.getMessage());
                }
            }
            return true;
        } catch (Exception e) {
            log.error("删除文件夹失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据传入的文件名生成一个唯一的文件名
     * <p>
     * 如果前缀和后缀均不为空，则组成有后缀的唯一文件名【日期目录 + 时间戳 + 文件前缀名 + 文件后缀名】
     * 如果前缀不为空，则组成无后缀的唯一文件名【日期目录 + 时间戳 + 文件前缀名】
     * 如果前缀后缀均为空，则直接将时间戳作为唯一文件名【日期目录 + 时间戳】
     *
     * @param fileName 文原始文件名
     * @return 唯一文件名【日期 + 文件前缀名 + 文件后缀名】
     */
    private static String getUniqueFilePath(String fileName) {
        // 获取当前日期，作为文件夹名
        LocalDate today = LocalDate.now();
        String dateCatalog = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 分离文件前缀和后缀
        int suffixIndex = fileName.lastIndexOf(".");
        // 如果找到了后缀分隔符，则获取前缀，否则使用整个文件名作为前缀
        String filePrefix = suffixIndex > 0 ? fileName.substring(0, suffixIndex) : fileName;
        // 如果找到了后缀分隔符，则获取后缀，否则为空字符串
        String fileSuffix = suffixIndex > 0 ? fileName.substring(suffixIndex + 1) : "";

        // 如果前缀和后缀均不为空，则组成有后缀的唯一文件名【日期目录 + 时间戳 + 文件前缀名 + 文件后缀名】
        if (!StringUtils.isEmpty(filePrefix) && !StringUtils.isEmpty(fileSuffix)) {
            // 组成唯一文件名
            StringBuilder builder = new StringBuilder();
            builder.append(dateCatalog).append("/")
                    .append(timestamp).append("-")
                    .append(filePrefix).append(".")
                    .append(fileSuffix);
            return builder.toString();
        } else if (!StringUtils.isEmpty(filePrefix)) {
            // 如果前缀不为空，则组成无后缀的唯一文件名【日期目录 + 时间戳 + 文件前缀名】
            StringBuilder builder = new StringBuilder();
            builder.append(dateCatalog).append("/")
                    .append(timestamp).append("-")
                    .append(filePrefix);
            return builder.toString();
        } else {
            // 如果前缀后缀均为空，则直接将时间戳作为唯一文件名【日期目录 + 时间戳】
            StringBuilder builder = new StringBuilder();
            builder.append(dateCatalog).append("/")
                    .append(timestamp).append("-");
            return builder.toString();
        }
    }

    /**
     * 使用文件URL获取对象（文件）名称
     *
     * @param url 文件URL
     * @return 对象全路径
     */
    @Override
    public String getObjectNameByUrl(String url) {
        try {
            // 解码URL
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name());

            // 去掉MinIO服务端点和存储桶名称的部分
            String objectName = decodedUrl.replaceFirst(minioAutoProperties.getEndpoint() + "/" + minioAutoProperties.getBucket() + "/", "");
            // 去掉对象名前面的路径，只保留最后一个斜杠后面的文件名
            return objectName.substring(objectName.lastIndexOf("/") + 1);
        } catch (Exception e) {
            log.error("无法从URL获取对象名称: {}", url, e);
            throw new RuntimeException("无法从URL获取对象名称！", e);
        }
    }

    /**
     * 根据 URL 解析出全局默认桶文件目录
     *
     * @param url 需要解析的 URL
     * @return 文件目录
     */
    @Override
    public String getObjectPathByUrl(String url) {
        String objectPathFromUrl = this.getObjectPathByUrl(minioAutoProperties.getBucket(), url);
        return objectPathFromUrl;
    }

    /**
     * 根据 URL 解析出文件目录
     *
     * @param bucketName 桶名称
     * @param url        需要解析的 URL
     * @return 文件目录
     */
    @Override
    public String getObjectPathByUrl(String bucketName, String url) {
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("解析 URL 失败，URL：{}，错误信息：{}", url, e.getMessage());
            throw new RuntimeException("解析 URL 失败");
        }

        // 返回对象名称
        return url.substring(url.indexOf(bucketName) + bucketName.length() + 1);
    }

    /**
     * 从MinIO对象存储中下载分块文件，然后将其写入本地临时文件，并返回临时文件数组。
     *
     * @param bucketName      MinIO中的存储桶名称
     * @param chunkFileFolder 分块文件在MinIO存储桶中的路径
     * @param chunkTotal      分块文件的总数
     * @return 临时文件数组
     */
    @Override
    public File[] downloadChunkFile(String bucketName, String chunkFileFolder, int chunkTotal) {
        // 创建一个大小为chunkTotal的文件数组
        File[] files = new File[chunkTotal];

        // 依次下载每个分块文件并写入到临时文件中
        for (int i = 0; i < chunkTotal; i++) {
            // 获取分块文件路径
            String chunkFilePath = chunkFileFolder + i;
            File chunkFile = null;
            try {
                // 创建临时的分块文件
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (Exception e) {
                log.error("创建临时分块文件失败：{}", e.getMessage());
                throw new RuntimeException("创建临时分块文件失败");
            }

            // 下载分块文件到本地临时文件
            try (FileOutputStream fileOutputStream = new FileOutputStream(chunkFile);
                 InputStream inputStream = minioClient.getObject(
                         GetObjectArgs
                                 .builder()
                                 .bucket(bucketName)
                                 .object(chunkFilePath)
                                 .build()
                 )
            ) {
                // 使用IOUtils工具类的copy方法将输入流中的内容拷贝到输出流中
                IOUtils.copy(inputStream, fileOutputStream);
            } catch (Exception e) {
                log.error("下载文件失败：{}" + chunkFile.getAbsolutePath(), e.getMessage());
                throw new RuntimeException("下载文件失败");
            }
            // 将该分块文件的临时文件对象添加到文件数组中
            files[i] = chunkFile;
        }
        // 返回临时文件数组
        return files;
    }


    /**
     * 清除指定路径下的所有分块文件
     *
     * @param bucketName 文件桶名称
     * @param path       分块文件的路径
     * @param chunkTotal 分块文件总数
     */
    @Override
    public void clearChunkFiles(String bucketName, String path, int chunkTotal) {
        // 组装需要删除的分块文件列表
        Iterable<DeleteObject> sources = Stream.iterate(0, i -> ++i) // 使用iterate()方法生成从0开始的连续整数流
                .limit(chunkTotal) // 对流进行截取，截取前chunkTotal个元素
                .map(i -> new DeleteObject(path + i)) // 为每个元素（即分块文件）添加前缀path，并封装成DeleteObject对象
                .collect(Collectors.toList()); // 将所有DeleteObject对象收集为一个List集合

        // 构造删除分块文件的参数对象
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucketName) // 设置所在的Bucket名称
                .objects(sources) // 设置要删除的分块文件列表
                .build();

        // 删除分块文件
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);

        // 遍历删除结果
        for (Result<DeleteError> f : results) {
            try {
                f.get(); // 获取当前分块文件的删除结果。如果删除成功，get()方法返回null；否则抛出异常
            } catch (Exception e) {
                log.error("删除分块文件失败，错误信息：{}", e.getMessage());
                throw new RuntimeException("删除分块文件失败"); // 如果删除失败，则抛出RuntimeException异常
            }
        }
    }


}
