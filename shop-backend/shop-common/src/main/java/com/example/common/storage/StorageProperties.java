package com.example.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 七牛云对象存储配置
 * 对应 application.yml 中 qiniu.* 配置项
 */
@Component
@ConfigurationProperties(prefix = "qiniu")
public class StorageProperties {

    /** AccessKey，七牛云控制台 -> 密钥管理获取 */
    private String accessKey;

    /** SecretKey */
    private String secretKey;

    /** 存储空间名 */
    private String bucket;

    /** 绑定的访问域名（CDN 或七牛域名），如 https://cdn.example.com，末尾不要带斜杠 */
    private String domain;

    /**
     * 存储区域，对应七牛云机房：
     * region0=华东  region1=华北  region2=华南
     * regionNa0=北美  regionAs0=东南亚  auto=自动（推荐）
     */
    private String region = "auto";

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
