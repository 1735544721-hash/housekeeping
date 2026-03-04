package org.example.springboot.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 应用私钥
     */
    private String privateKey;
    
    /**
     * 支付宝公钥
     */
    private String publicKey;
    
    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;
    
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    
    /**
     * 同步跳转地址
     */
    private String returnUrl;
    
    /**
     * 字符集
     */
    private static final String CHARSET = "UTF-8";
    
    /**
     * 签名类型
     */
    private static final String SIGN_TYPE = "RSA2";
    
    /**
     * 返回格式
     */
    private static final String FORMAT = "json";
    
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
            gatewayUrl,
            appId,
            privateKey,
            FORMAT,
            CHARSET,
            publicKey,
            SIGN_TYPE
        );
    }
    
    public String getCharset() {
        return CHARSET;
    }
    
    public String getSignType() {
        return SIGN_TYPE;
    }
    
    public String getFormat() {
        return FORMAT;
    }
}


