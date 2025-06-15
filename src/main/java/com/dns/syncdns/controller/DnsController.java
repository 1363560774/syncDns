package com.dns.syncdns.controller;

import com.dns.syncdns.domain.Domain;
import com.dns.syncdns.domain.DomainRecords;
import com.dns.syncdns.service.DnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DnsController {

    @Autowired
    private DnsService dnsService;

    //获取dns列表
    @GetMapping("dns.list.do")
    public Domain loadDnsDescribeDomainRecords() {
        return dnsService.loadDnsDescribeDomainRecords();
    }

    //手动同步dns
    @PostMapping("dns.manual.do")
    public void manualSynchronizationDns(DomainRecords domainRecords) {
        dnsService.manualSynchronizationDns(domainRecords);
    }

    //获取公网ip
    @GetMapping("public.ip.do")
    public String loadPublicIp() {
        return dnsService.loadPublicIp();
    }

    //每10分钟执行一次
    @Scheduled(fixedDelay = 1000 * 60 * 10)
    private void myTasks() {
        System.out.println("定时任务执行");
        dnsService.syncDns();
    }
}
