package com.example.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置（沙箱 + 公钥模式）
 * <p>
 * 配置项见 application.yml 的 alipay.* 节点：
 *   app-id      沙箱应用 AppID
 *   private-key 应用私钥（PKCS8 格式，去除首尾标记与换行后填入）
 *   public-key  支付宝公钥（沙箱页面「支付宝公钥」一栏）
 *   gateway     沙箱网关 https://openapi.alipaydev.com/gateway.do
 *   notify-url  异步通知地址（需公网可访问）
 *   return-url  同步跳转地址（支付完浏览器跳回前端）
 *   sign-type   RSA2
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    private String appId;
    private String privateKey;
    private String publicKey;
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";

    /**
     * 公钥模式构造 AlipayClient
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gateway, appId, privateKey, "json", "UTF-8", publicKey, signType);
    }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
}
