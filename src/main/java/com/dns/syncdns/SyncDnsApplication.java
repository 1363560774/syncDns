package com.dns.syncdns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class SyncDnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncDnsApplication.class, args);
    }

}
