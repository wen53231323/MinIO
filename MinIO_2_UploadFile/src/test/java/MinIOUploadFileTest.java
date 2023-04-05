import io.minio.*;
import io.minio.errors.MinioException;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * @author wen
 * @version 1.0
 * @description TODO 测试使用MinIO
 * @date 2023/3/7 14:48
 */
public class MinIOUploadFileTest {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        // 上传文件测试
        // upload("testbucket", "imgs/1.png", "E:\\音、视、图\\图片\\静态图片\\1.png");
        // upload("testbucket", "mp4/1.mp4", "E:\\音、视、图\\视频\\视频壁纸\\Bongo-Cat-Christmas.mp4");

        // 下载文件测试
        // getFile("testbucket", "imgs/1.png", "E:\\test.png");
        // getFile("testbucket", "mp4/1.mp4", "E:\\test.mp4");

        // 删除文件测试
        // delete("testbucket", "imgs/1.png");
        // delete("testbucket", "mp4/1.mp4");
    }

    // 创建对象
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000") // 地址
                    .credentials("minioadmin", "minioadmin") // 用户名密码
                    .build();

    /**
     * TODO 上传文件
     *
     * @param bucket     桶的名称
     * @param filepath   上传到指定路径
     * @param uploadPath 本地文件路径
     */
    public static void upload(String bucket, String filepath, String uploadPath) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            // 检查testbucket桶是否创建，没有创建自动创建
            if (!found) {
                System.out.println("桶不存在，开始创建...");
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                System.out.println("桶创建完成...");
            } else {
                System.out.println("桶已经存在...");
            }
            System.out.println("开始上传文件...");
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket) // 指定桶
                            .object(filepath) // 指定上传的路径名和文件名
                            .filename(uploadPath) // 指定本地路径下的文件
                            .build());
            System.out.println("上传文件完毕...");
        } catch (MinioException e) {
            System.out.println("出现错误: " + e);
            System.out.println("HTTP 追踪: " + e.httpTrace());
        }
    }

    /**
     * TODO 下载文件
     *
     * @param bucket   桶的名称
     * @param filepath 文件路径
     * @param outFile  下载本地文件路径
     */
    public static void getFile(String bucket, String filepath, String outFile) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            System.out.println("开始下载...");
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)// 指定桶
                            .object(filepath)// 指定文件路径
                            .build()
            );
            // 指定输出路径
            FileOutputStream fileOutputStream = new FileOutputStream(new File(outFile));
            // 从流中读取数据
            IOUtils.copy(stream, fileOutputStream);
            System.out.println("下载成功...");

        } catch (MinioException e) {
            System.out.println("出现错误: " + e);
            System.out.println("HTTP 追踪: " + e.httpTrace());
        }
    }

    /**
     * TODO 删除文件
     *
     * @param bucket   桶的名称
     * @param filepath 文件路径
     */
    public static void delete(String bucket, String filepath) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            System.out.println("开始删除...");
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)// 指定桶
                            .object(filepath)// 指定文件路径
                            .build()
            );
            System.out.println("删除成功...");
        } catch (MinioException e) {
            System.out.println("出现错误: " + e);
            System.out.println("HTTP 追踪: " + e.httpTrace());
        }
    }


    /**
     * TODO 查询文件
     */
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")// 指定桶
                .object("1.mp4")// 指定文件路径
                .build();
        try (
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\develop\\upload\\1_1.mp4"));
        ) {
            if (inputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (Exception e) {
        }

    }
}


