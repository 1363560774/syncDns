package com.dns.syncdns.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.tea.utils.StringUtils;
import com.aliyun.teaopenapi.Client;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teaopenapi.models.OpenApiRequest;
import com.aliyun.teaopenapi.models.Params;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dns.syncdns.common.OptionEnum;
import com.dns.syncdns.common.RequestUtil;
import com.dns.syncdns.domain.AccessKey;
import com.dns.syncdns.domain.Domain;
import com.dns.syncdns.domain.DomainRecords;
import com.dns.syncdns.domain.Properties;
import com.dns.syncdns.service.DnsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aliyun.openapiutil.Client.query;

@Slf4j
@Service
public class DnsServiceImpl implements DnsService {

    @Autowired
    private AccessKey accessKey;

    @Autowired
    private Properties properties;

    @Override
    public Domain loadDnsDescribeDomainRecords() {
        try {
            // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
            Client client = createClient(accessKey.getId(), accessKey.getSecret());
            Params params = createApiInfo(OptionEnum.DescribeDomainRecords);
            // query params
            Map<String, Object> queries = new HashMap<>();
            queries.put("DomainName", "zhaokai96.com");
            // runtime options
            RuntimeOptions runtime = new RuntimeOptions();
            OpenApiRequest request = new OpenApiRequest().setQuery(query(queries));
            // 复制代码运行请自行打印 API 的返回值
            // 返回值为 Map 类型，可从 Map 中获得三类数据：响应体 body、响应头 headers、HTTP 返回的状态码 statusCode。
            Map<String, Object> resp = Common.anyifyMapValue(client.callApi(params, request, runtime));
            String jsonString = Common.toJSONString(resp.get("body"));
            return JSONObject.parseObject(jsonString, Domain.class);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public void manualSynchronizationDns(DomainRecords domainRecords) {
        try {
            // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
            Client client = createClient(accessKey.getId(), accessKey.getSecret());
            Params params = createApiInfo(OptionEnum.UpdateDomainRecord);
            // query params
            Map<String, Object> queries = new HashMap<>();
            queries.put("RecordId", domainRecords.getRecordId());
            queries.put("RR", domainRecords.getRR());
            queries.put("Type", domainRecords.getType());
            queries.put("Value", domainRecords.getValue());
            // runtime options
            RuntimeOptions runtime = new RuntimeOptions();
            OpenApiRequest request = new OpenApiRequest().setQuery(query(queries));
            // 复制代码运行请自行打印 API 的返回值
            // 返回值为 Map 类型，可从 Map 中获得三类数据：响应体 body、响应头 headers、HTTP 返回的状态码 statusCode。
            Map<String, Object> resp = Common.anyifyMapValue(client.callApi(params, request, runtime));
            String jsonString = Common.toJSONString(resp);
            log.info(jsonString);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public String loadPublicIp() {
        String url = "https://www.ipplus360.com/getIP";
        String s = RequestUtil.doGetRequest(url, new HttpHeaders());
        return JSONObject.parseObject(s).get("data").toString();
    }

    @Override
    public void syncDns() {
        String publicIp = loadPublicIp();
        List<String> domainName = properties.getDomainName();
        domainName.forEach(name -> {
            oneSyncDns(name, publicIp);
        });
    }

    private void oneSyncDns(String domainName, String publicIp) {
        Domain domain = loadDnsDescribeDomainRecords();
        List<DomainRecords> record = domain.getDomainRecords().get("Record");
        List<String> types = properties.getTypes();
        String status = properties.getStatus();
        record.stream().filter(d ->
                !d.getValue().equals(publicIp)
                        && status.equals(d.getStatus())
                        && !StringUtils.isEmpty(d.getRecordId())
                        && domainName.equals(d.getDomainName())
                        && types.contains(d.getType())
        ).forEach(v-> {
            log.info("修改前的阿里云域名解析详情 " + v);
            v.setValue(publicIp);
            manualSynchronizationDns(v);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = simpleDateFormat.format(new Date());
            log.info(format + " I do myself per 10 minutes publicIp = " + publicIp);
        });
    }

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Alidns
        config.endpoint = "alidns.cn-shanghai.aliyuncs.com";
        return new Client(config);
    }

    /**
     * 使用STS鉴权方式初始化账号Client，推荐此方式。
     * @param accessKeyId
     * @param accessKeySecret
     * @param securityToken
     * @return Client
     * @throws Exception
     */
    public Client createClientWithSTS(String accessKeyId, String accessKeySecret, String securityToken) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret)
                // 必填，您的 Security Token
                .setSecurityToken(securityToken)
                // 必填，表明使用 STS 方式
                .setType("sts");
        // Endpoint 请参考 https://api.aliyun.com/product/Alidns
        config.endpoint = "alidns.cn-shanghai.aliyuncs.com";
        return new Client(config);
    }

    /**
     * API 相关
     * @return OpenApi.Params
     */
    public Params createApiInfo(OptionEnum optionEnum) {
        return new com.aliyun.teaopenapi.models.Params()
                // 接口名称
                .setAction(optionEnum.name())
                // 接口版本
                .setVersion("2015-01-09")
                // 接口协议
                .setProtocol("HTTPS")
                // 接口 HTTP 方法
                .setMethod("POST")
                .setAuthType("AK")
                .setStyle("RPC")
                // 接口 PATH
                .setPathname("/")
                // 接口请求体内容格式
                .setReqBodyType("json")
                // 接口响应体内容格式
                .setBodyType("json");
    }
}
