package com.dns.syncdns.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kai
 * @date 2025/6/15 20:58
 */

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("remote")
public class RemoteServerProperties {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String rsa;
    private String remoteFilePath;
}
