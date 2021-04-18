package ru.tsystems.tchallenge.service.configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tsystems.tchallenge.service.security.authentication.VkVerifier;

@Configuration
public class VkConfig {
    @Value("${vk.secret-key}")
    private String secretKey;

    @Bean
    public VkVerifier vkVerifier() {
        return VkVerifier.builder()
                .secretKey(secretKey)
                .build();
    }
}
