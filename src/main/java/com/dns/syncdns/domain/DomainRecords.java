package com.dns.syncdns.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DomainRecords {

    private String RR;
    private String Line;
    private String Status;
    private Boolean Locked;
    private String Type;
    private String DomainName;
    private String Value;
    private String RecordId;
    private Long TTL;
}
