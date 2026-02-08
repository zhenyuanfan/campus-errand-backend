package com.yuan.campuserrandbackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.yuan.campuserrandbackend.config.CosConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Objects;

@Component
public class CosManager {

    @Resource
    private CosConfig cosConfig;

    public String upload(InputStream inputStream, long contentLength, String contentType, String key) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            if (contentLength > 0) {
                metadata.setContentLength(contentLength);
            }
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), key, inputStream, metadata);
            cosClient.putObject(putObjectRequest);
        } finally {
            cosClient.shutdown();
        }

        String domain = cosConfig.getDomain();
        if (domain == null || domain.trim().isEmpty()) {
            domain = String.format("https://%s.cos.%s.myqcloud.com", cosConfig.getBucket(), cosConfig.getRegion());
        }
        domain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        key = Objects.requireNonNull(key);
        key = key.startsWith("/") ? key.substring(1) : key;
        return domain + "/" + key;
    }
}
