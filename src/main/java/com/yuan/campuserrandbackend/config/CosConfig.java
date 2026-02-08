package com.yuan.campuserrandbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.cos")
public class CosConfig {

    private String secretId;

    private String secretKey;

    private String region;

    private String bucket;

    private String domain;

    private String avatarPrefix;
}
