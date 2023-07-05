package com.dns.syncdns.common;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RequestUtil {

    public static String doGetRequest(String url, HttpHeaders headers) {

        RestTemplate client = new RestTemplate();
        HttpMethod method = HttpMethod.GET;
        HttpEntity<String> requestEntity = new HttpEntity<>("parameters", headers);

        // 执行HTTP请求，将返回的结构使用String类格式化
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
        return response.getBody();
    }
}
