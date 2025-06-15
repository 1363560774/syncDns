package com.dns.syncdns.config;

import com.aliyun.cas20200407.Client;
import com.aliyun.teaopenapi.models.Config;
import com.dns.syncdns.domain.AccessKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author kai
 * @date 2025/6/14 13:41
 */

@Component
public class ClientConfig {

    @Autowired
    private AccessKey accessKey;

    @Bean
    Client casClient() {
        try {
            Config config = new Config()
                    // 必填，您的 AccessKey ID
                    .setAccessKeyId(accessKey.getId())
                    // 必填，您的 AccessKey Secret
                    .setAccessKeySecret(accessKey.getSecret())
                    .setEndpoint("cas.aliyuncs.com");
            // Endpoint 请参考 https://api.aliyun.com/product/Alidns
//            config.endpoint = "alidns.cn-shanghai.aliyuncs.com";
            return new Client(config);
        } catch (Exception e) {
            System.exit(1);
        }
        return null;
    }
}
