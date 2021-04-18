package ru.tsystems.tchallenge.service.configuration.codemaster;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.tsystems.tchallenge.codemaster.ApiClient;
//import ru.tsystems.tchallenge.codemaster.api.ContestsApi;
import ru.tsystems.tchallenge.codemaster.api.LanguagesApi;
import ru.tsystems.tchallenge.codemaster.api.SubmissionsApi;

import javax.annotation.PostConstruct;

@Configuration
public class CodemasterConfig {

    @Value("${tchallenge.codemaster.url}")
    private String baseUrl;

    private ApiClient apiClient;

    @PostConstruct
    public  void init() {
        apiClient = new ApiClient().setBasePath(baseUrl);
    }
/*
    @Bean
    ContestsApi contestsApi() {
        return new ContestsApi(apiClient);
    }
*/
    @Bean
    SubmissionsApi submissionsApi() {
        return new SubmissionsApi(apiClient);
    }

    @Bean
    LanguagesApi languagesApi() {
        return new LanguagesApi(apiClient);
    }
}
