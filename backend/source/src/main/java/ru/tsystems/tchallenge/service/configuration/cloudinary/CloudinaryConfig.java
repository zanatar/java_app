package ru.tsystems.tchallenge.service.configuration.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.google.common.base.Strings;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Value("${tchallenge.proxy.host}")
    private String proxyHost;

    @Value("${tchallenge.proxy.port}")
    private String proxyPort;


    @Bean
    public Cloudinary cloudinary() {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
        if (!Strings.isNullOrEmpty(proxyHost)) {
            cloudinary.config.proxyHost = proxyHost;
            cloudinary.config.proxyPort = Integer.parseInt(proxyPort);
        }

        return cloudinary;
    }
}
