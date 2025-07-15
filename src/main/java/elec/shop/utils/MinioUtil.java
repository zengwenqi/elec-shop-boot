package elec.shop.utils;

import cn.hutool.core.util.ObjectUtil;
import io.minio.Result;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * @author YHL
 * @date 2024/4/24 9:51
 * @description
 */
@Component
@Slf4j
public class MinioUtil {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            log.error("初始化MinIO客户端失败", e);
            throw new RuntimeException("初始化MinIO客户端失败", e);
        }
    }

    /**
     * description: 判断bucket是否存在，不存在则创建
     */
    @SneakyThrows
    public boolean existBucket(String name) {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
        }
        return exists;
    }

    /**
     * 创建存储bucket
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    @SneakyThrows
    public Boolean makeBucket(String bucketName) {
        boolean exist = existBucket(bucketName);
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        }
        return false;
    }

    /**
     * 删除存储bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    @SneakyThrows
    public Boolean removeBucket(String bucketName) {
        boolean exist = existBucket(bucketName);
        if (!exist) return false;

        Iterable<Result<Item>> results =
                minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        for (Result<Item> result : results) {
            Item item = result.get();
            // 桶不为空不允许删除
            if (item.size() > 0) {
                return false;
            }
        }
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket(bucketName)
                .build());
        return true;
    }

    /**
     * 列出所有存储桶
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 列出所有存储桶名称
     */
    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();
        if (ObjectUtil.isEmpty(bucketList))
            return null;
        List<String> bucketListName = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    /**
     * 列出存储桶中的所有对象名称
     *
     * @param bucketName 存储桶名称
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        boolean exist = existBucket(bucketName);
        if (!exist) return null;

        List<String> listObjectNames = new ArrayList<>();
        Iterable<Result<Item>> results =
                minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        for (Result<Item> result : results) {
            Item item = result.get();
            listObjectNames.add(item.objectName());
        }
        return listObjectNames;
    }

    /**
     * 查看文件对象
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
    public Map<String, Object> listObjects(String bucketName) {
        boolean exist = existBucket(bucketName);
        if (!exist) return null;

        Iterable<Result<Item>> results =
                minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        Map<String, Object> map = new HashMap<>();
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                map.put(item.objectName(), item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }

    /**
     * 文件访问路径
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     */
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        boolean exist = existBucket(bucketName);
        if (!exist) return null;

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(2, TimeUnit.MINUTES)
                        .build());
    }

    /**
     * 删除一个对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     */
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        boolean exist = existBucket(bucketName);
        if (!exist) return false;

        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        return true;
    }

    /**
     * 删除指定桶的多个文件对象
     *
     * @param bucketName  存储桶名称
     * @param objectNames 含有要删除的多个object名称的迭代器对象
     */
    @SneakyThrows
    public boolean removeObject(String bucketName, List<String> objectNames) {
        boolean exist = existBucket(bucketName);
        if (!exist) return false;

        List<DeleteObject> objects = new LinkedList<>();
        for (String objectName : objectNames) {
            objects.add(new DeleteObject(objectName));
        }
        minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
        return true;
    }

    /**
     * 批量删除文件对象
     *
     * @param bucketName 存储bucket名称
     * @param objects    对象名称集合
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).collect(Collectors.toList());
        return minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
    }

    /**
     * 文件上传
     */
    public String upload(MultipartFile file, String name) {
        try {
            // 生成文件名
            String fileName = UUID.randomUUID().toString() +
                    file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            fileName = name + "/" + fileName;
            // 设置文件类型
            String contentType = file.getContentType();

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            return fileName;
        } catch (Exception e) {
            log.error("上传文件到MinIO失败", e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 文件下载
     *
     * @param fileName 文件名
     * @param delete   是否删除
     */
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response) {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                String data = "文件下载失败";
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes(StandardCharsets.UTF_8));
                return;
            }

            outputStream = response.getOutputStream();
            // 获取文件对象
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
            byte[] buf = new byte[1024];
            int length = 0;
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName.substring(fileName.lastIndexOf("/") + 1), "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            // 输出文件
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            inputStream.close();
            // 判断：下载后是否同时删除minio上的存储文件
            if (BooleanUtils.isTrue(delete)) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            }
        } catch (Throwable ex) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            try {
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                outputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示文件
     * @param fileName
     * @param response
     */
    public void getFile(String fileName, HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                String data = "文件不存在";
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes(StandardCharsets.UTF_8));
                return;
            }

            // 获取文件对象
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
            String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            String contentType = getContentTypeForExtension(fileExtension);
            if (contentType == null) {
                // 如果无法识别文件类型，设置一个默认的通用类型或者返回错误提示等
                contentType = "application/octet-stream";
            }
            response.reset();
            response.setHeader("Content-Type", contentType);
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
        } catch (Throwable ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件获取失败";
            try {
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (outputStream!= null) {
                    outputStream.close();
                }
                if (inputStream!= null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 辅助方法
     * @param extension
     * @return
     */
    private String getContentTypeForExtension(String extension) {
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            // 根据实际需要添加更多图片类型的映射
            default:
                return null;
        }
    }

    public void remove(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            log.error("从MinIO删除文件失败", e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    public InputStream download(String fileName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            log.error("从MinIO下载文件失败", e);
            throw new RuntimeException("下载文件失败", e);
        }
    }

    /**
     * 获取文件预览URL
     * @param objectName 文件名
     * @return 预览URL
     */
    public String getPreviewUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) return null;
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)  // URL有效期7天
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件预览URL失败:", e);
            return null;
        }
    }

    /**
     * 获取文件下载URL
     * @param objectName 文件名
     * @return 下载URL
     */
    public String getDownloadUrl(String objectName) {
        try {
            // 设置响应头参数
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-type", "application/octet-stream");
            reqParams.put("response-content-disposition", "attachment; filename=\"" +
                objectName.substring(objectName.lastIndexOf("/") + 1) + "\"");

            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(1, TimeUnit.HOURS)  // URL有效期1小时
                    .extraQueryParams(reqParams)  // 添加强制下载的响应头
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件下载URL失败:", e);
            return null;
        }
    }

    /**
     * 判断 MinIO 中指定存储桶、指定对象（文件）是否存在
     * @param bucketName 存储桶名称
     * @param objectName 对象（文件）名称
     * @return true：存在；false：不存在
     */
    @SneakyThrows
    public boolean isObjectExist(String bucketName, String objectName) {
        try {
            // 调用 statObject 方法，若对象存在则返回 StatObjectResponse，不存在则抛出异常
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            // 能执行到这里，说明对象存在
            return true;
        } catch (Exception e) {
            // 捕获到异常，判断是否是对象不存在的异常（不同版本 MinIO 客户端异常类型可能有差异，可更精细判断）
            // 简单处理：只要抛出异常就认为对象不存在，实际可根据异常信息细化，比如 MinioException 的 errorCode 等
            log.debug("文件 {} 在存储桶 {} 中不存在，异常信息：", objectName, bucketName, e);
            return false;
        }
    }

    public Map<String, String> getObjectUrls(List<String> objectNames) {
        // 新增：如果输入的列表为null，直接返回null
        if (objectNames == null || objectNames.equals("")) {
            return null;
        }
        return objectNames.stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> getObjectUrl("elec-shop", name),
                        (existing, replacement) -> existing
                ));
    }
}
