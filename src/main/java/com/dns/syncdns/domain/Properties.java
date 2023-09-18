package com.dns.syncdns.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("properties")
public class Properties {

    private String status;
    private List<String> domainName;
    private List<String> types;
}
