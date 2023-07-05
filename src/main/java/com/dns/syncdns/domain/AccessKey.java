package com.dns.syncdns.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("dns.access.key")
public class AccessKey {

    private String id;
    private String secret;
}
