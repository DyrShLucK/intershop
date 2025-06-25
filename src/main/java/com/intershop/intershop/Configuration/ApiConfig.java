package com.intershop.intershop.Configuration;

import com.intershop.intershop.ApiClient;
import com.intershop.intershop.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class ApiConfig {
    @Bean
    public ApiClient apiClient(@Value("${payment.service.url}") String baseUrl) {
        return new ApiClient(WebClient.builder().baseUrl(baseUrl).build()).setBasePath(baseUrl);
    }

    @Bean
    public DefaultApi defaultApi(
            @Value("${payment.service.url}") String baseUrl,
            WebClient.Builder webClientBuilder,
            ReactiveOAuth2AuthorizedClientManager clientManager,
            @Value("${oauth2.client.registration-id}") String clientId
    ) {
        ApiClient apiClient = new ApiClient(
                webClientBuilder
                        .baseUrl(baseUrl)
                        .filter(oauthFilter(clientManager, clientId))
                        .build()
        );
        return new DefaultApi(apiClient);
    }

    private ExchangeFilterFunction oauthFilter(
            ReactiveOAuth2AuthorizedClientManager clientManager,
            String clientId
    ) {
        return (request, next) ->
                getAccessToken(clientManager, clientId)
                        .flatMap(token -> next.exchange(
                                withBearerToken(request, token)
                        ));
    }

    private Mono<String> getAccessToken(
            ReactiveOAuth2AuthorizedClientManager clientManager,
            String clientId
    ) {
        return clientManager.authorize(
                        OAuth2AuthorizeRequest.withClientRegistrationId(clientId)
                                .principal("system")
                                .build()
                )
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }

    private ClientRequest withBearerToken(
            ClientRequest request,
            String token
    ) {
        return ClientRequest.from(request)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }
}
