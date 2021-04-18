package ru.tsystems.tchallenge.service.configuration;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class RestConfig {

    @Value("${tchallenge.proxy.host}")
    private String proxyHost;

    @Value("${tchallenge.proxy.port}")
    private String proxyPort;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (!Strings.isNullOrEmpty(proxyHost)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
            requestFactory.setProxy(proxy);
        }

        return new RestTemplate(requestFactory);
    }
}
