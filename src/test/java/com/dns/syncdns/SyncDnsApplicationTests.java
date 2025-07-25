package com.dns.syncdns;

import com.dns.syncdns.service.DnsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SyncDnsApplicationTests {

    @Autowired
    private DnsService dnsService;

    @Test
    void contextLoads() {
        dnsService.loadCasDescribeDomainRecords();
        Boolean b = dnsService.downloadCertificateDeployToService(18855519L);
        System.out.println(b);
    }

}
