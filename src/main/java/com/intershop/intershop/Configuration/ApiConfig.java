package com.intershop.intershop.Configuration;

import com.intershop.intershop.ApiClient;
import com.intershop.intershop.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiConfig {
    @Bean
    public ApiClient apiClient(@Value("${payment.service.url}") String baseUrl) {
        return new ApiClient(WebClient.builder().baseUrl(baseUrl).build()).setBasePath(baseUrl);
    }

    @Bean
    public DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}
