package ru.tsystems.tchallenge.service.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Strings;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleConfig {
    @Value("${google.client-id}")
    private String clientId;

    @Value("${tchallenge.proxy.host}")
    private String proxyHost;

    @Value("${tchallenge.proxy.port}")
    private String proxyPort;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        ApacheHttpTransport.Builder builder = new ApacheHttpTransport.Builder();
        if (!Strings.isNullOrEmpty(proxyHost)) {
            builder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
        }
        ApacheHttpTransport httpTransport = builder.build();
        return new GoogleIdTokenVerifier.Builder(httpTransport,
                JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

}
