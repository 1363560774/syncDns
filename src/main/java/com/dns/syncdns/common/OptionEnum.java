package com.dns.syncdns.common;

/**
 * 解析记录枚举
 */
public enum OptionEnum {

    AddDomainRecord,//根据传入参数添加解析记录
    DeleteDomainRecord,//根据传入参数删除解析记录
    DeleteSubDomainRecords,//根据传入参数删除主机记录对应的解析记录
    UpdateDomainRecord,//根据传入参数修改解析记录
    UpdateDomainRecordRemark,//根据传入参数修改解析记录的备注
    SetDomainRecordStatus,//根据传入参数设置解析记录状态
    DescribeDomainRecordInfo,//获取解析记录的详细信息
    DescribeDomainRecords,//获取解析记录列表
    DescribeRecordLogs,//根据传入参数获取域名的解析操作日志
    DescribeSubDomainRecords,//根据传入参数获取某个固定子域名的所有解析记录列表
    GetTxtRecordForVerify,//生成txt记录。用于域名、子域名找回、添加子域名验证、批量找回等功能
}
