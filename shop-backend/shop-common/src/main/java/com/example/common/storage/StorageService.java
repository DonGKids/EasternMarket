package com.example.common.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 七牛云对象存储上传服务
 * 统一负责头像、商品图等图片上传到七牛云 Kodo
 */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private StorageProperties props;

    private UploadManager uploadManager;
    private Auth auth;
    private volatile boolean ready = false;

    @PostConstruct
    public void init() {
        // 凭证未配置时跳过初始化，保证服务能正常启动，仅上传接口不可用
        if (isBlank(props.getAccessKey()) || isBlank(props.getSecretKey())
                || isBlank(props.getBucket()) || isBlank(props.getDomain())) {
            log.warn("七牛云未配置完整（accessKey/secretKey/bucket/domain），上传功能不可用");
            return;
        }
        Configuration cfg = new Configuration(parseRegion(props.getRegion()));
        this.uploadManager = new UploadManager(cfg);
        this.auth = Auth.create(props.getAccessKey(), props.getSecretKey());
        this.ready = true;
        log.info("七牛云存储服务初始化完成，bucket={}, domain={}", props.getBucket(), props.getDomain());
    }

    /**
     * 上传文件字节流到七牛云
     *
     * @param data      文件字节数组
     * @param ext       扩展名（如 jpg / .jpg），可为空
     * @param dirPrefix 存储路径前缀，如 avatar/ 或 product/，需以 / 结尾
     * @return 可直接访问的完整 URL
     */
    public String upload(byte[] data, String ext, String dirPrefix) {
        if (!ready) {
            throw new IllegalStateException("七牛云未配置，无法上传");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("文件内容为空");
        }

        // 归一化扩展名 -> .jpg
        String extension = normalizeExt(ext);
        // 归一化目录前缀 -> avatar/
        String prefix = normalizePrefix(dirPrefix);
        // 生成全局唯一的 object key
        String key = prefix + UUID.randomUUID().toString().replace("-", "") + extension;

        String upToken = auth.uploadToken(props.getBucket(), key);
        try {
            Response resp = uploadManager.put(data, key, upToken);
            if (!resp.isOK()) {
                throw new RuntimeException("七牛云上传失败：HTTP " + resp.statusCode
                        + " " + resp.bodyString());
            }
            String url = props.getDomain().replaceAll("/+$", "") + "/" + key;
            log.info("上传七牛云成功，key={}, size={}", key, data.length);
            return url;
        } catch (QiniuException e) {
            log.error("上传七牛云异常，key={}", key, e);
            throw new RuntimeException("上传七牛云失败：" + e.getMessage(), e);
        }
    }

    private Region parseRegion(String r) {
        if (r == null) return Region.autoRegion();
        return switch (r) {
            case "region0" -> Region.region0();   // 华东
            case "region1" -> Region.region1();   // 华北
            case "region2" -> Region.region2();   // 华南
            case "regionNa0" -> Region.regionNa0(); // 北美
            case "regionAs0" -> Region.regionAs0(); // 东南亚
            default -> Region.autoRegion();
        };
    }

    private String normalizeExt(String ext) {
        if (ext == null || ext.isBlank()) return "";
        ext = ext.trim().toLowerCase();
        if (ext.startsWith(".")) return ext;
        return "." + ext;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return "";
        prefix = prefix.trim();
        if (!prefix.endsWith("/")) prefix = prefix + "/";
        if (prefix.startsWith("/")) prefix = prefix.substring(1);
        return prefix;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
