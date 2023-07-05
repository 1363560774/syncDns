package com.dns.syncdns.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class Domain {

    private Integer TotalCount;
    private String RequestId;
    private Integer PageSize;
    private Integer PageNumber;

    Map<String, List<DomainRecords>> DomainRecords;
}
