package com.dns.syncdns.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.cas20200407.models.GetUserCertificateDetailResponse;
import com.aliyun.cas20200407.models.ListUserCertificateOrderResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.utils.StringUtils;
import com.aliyun.teaopenapi.Client;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teaopenapi.models.OpenApiRequest;
import com.aliyun.teaopenapi.models.Params;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dns.syncdns.common.OptionEnum;
import com.dns.syncdns.common.RequestUtil;
import com.dns.syncdns.domain.*;
import com.dns.syncdns.domain.Properties;
import com.dns.syncdns.service.DnsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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

    @Autowired
    private RemoteServerProperties remoteServer;

    @Autowired
    private com.aliyun.cas20200407.Client casClient;

    //占存当前IP 不一致时修改阿里云配置
    private static String IP;

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
        if (!publicIp.equals(IP)) {
            IP = publicIp;
        } else {
            return;
        }
        List<String> domainName = properties.getDomainName();
        domainName.forEach(name -> {
            oneSyncDns(name, IP);
        });
    }

    @Override
    public Boolean downloadCertificateDeployToService(Long certId) {
        com.aliyun.cas20200407.models.GetUserCertificateDetailRequest getUserCertificateDetailRequest = new com.aliyun.cas20200407.models.GetUserCertificateDetailRequest()
                .setCertId(certId);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            GetUserCertificateDetailResponse userCertificateDetailWithOptions = casClient.getUserCertificateDetailWithOptions(getUserCertificateDetailRequest, runtime);
            String pem = userCertificateDetailWithOptions.getBody().getCert();
            String key = userCertificateDetailWithOptions.getBody().getKey();
            String common = userCertificateDetailWithOptions.getBody().getCommon();
            //把 pem 和 key 写入到 resource/ca  目录下 temp.pem 和 temp.key
            createClientSendCommand(pem, key, common);
            log.info(userCertificateDetailWithOptions.body.toString());
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            String message = Common.assertAsString(error.message);
            log.error("error message: {}", message);
            return false;
        }
        return true;
    }

    /**
     * 将证书和私钥写入 resource/ca 目录下的 pem 和 key 文件中
     *
     * @param pem 证书内容
     * @param key 私钥内容
     */
    private void createClientSendCommand(String pem, String key, String common) {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        try {
            ClientSession session = client.connect(remoteServer.getUsername(), remoteServer.getHost(), remoteServer.getPort())
                    .verify(5000).getSession();
            // 1. 创建带密码的密钥提供器
            FilePasswordProvider passwordProvider = (sessionContext, namedResource, i) -> remoteServer.getPassword();
            // 2. 加载受密码保护的私钥
            FileKeyPairProvider keyProvider = new FileKeyPairProvider(Paths.get(remoteServer.getRsa()));
            keyProvider.setPasswordFinder(passwordProvider);
            // 3. 添加密钥身份
            session.addPublicKeyIdentity(keyProvider.loadKeys(session).iterator().next());
            // 4. 执行认证
            if (session.auth().verify(5000).isFailure()) {
                log.error("认证失败！");
                throw new RuntimeException("认证失败！");
            }
            log.info("带密码密钥认证成功！");
            String remoteFilePath = remoteServer.getRemoteFilePath();
            String commandKey = "echo -e \"" + key+ "\" > " + remoteFilePath+common + ".key";
            String commandPem = "echo -e \"" + pem + "\" > " + remoteFilePath+common + ".pem";
            executeCommand(session, commandKey);
            executeCommand(session, commandPem);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            client.stop();
        }
    }

    public void executeCommand(ClientSession session, String command) throws IOException {
        try (ChannelExec execChannel = session.createExecChannel(command);
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             ByteArrayOutputStream error = new ByteArrayOutputStream()) {
            execChannel.setOut(output);
            execChannel.setErr(error);
            execChannel.open().verify();
            // 等待命令执行完成（超时30秒）
            execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 30_000);
            // 检查退出状态码
            if (execChannel.getExitStatus() != 0) {
                //错误日志是ByteArrayOutputStream 转 utf8 打印出来
                System.out.println(new String(error.toByteArray(), StandardCharsets.UTF_8));
                throw new IOException("Command failed: " + error);
            }
        }
    }

    @Override
    public Domain loadCasDescribeDomainRecords() {
        com.aliyun.cas20200407.models.ListUserCertificateOrderRequest listUserCertificateOrderRequest = new com.aliyun.cas20200407.models.ListUserCertificateOrderRequest()
                .setStatus("ISSUED")
                .setOrderType("CERT");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            ListUserCertificateOrderResponse listUserCertificateOrderResponse = casClient.listUserCertificateOrderWithOptions(listUserCertificateOrderRequest, runtime);
            System.out.println(com.aliyun.teautil.Common.toJSONString(com.aliyun.teautil.Common.toMap(listUserCertificateOrderResponse)));
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
        }
        return null;
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
