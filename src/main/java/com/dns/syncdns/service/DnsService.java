package com.dns.syncdns.service;

import com.dns.syncdns.domain.Domain;
import com.dns.syncdns.domain.DomainRecords;

public interface DnsService {

    Domain loadDnsDescribeDomainRecords();

    void manualSynchronizationDns(DomainRecords domainRecords);

    String loadPublicIp();

    void syncDns();

    Boolean downloadCertificateDeployToService(Long certId);
}
