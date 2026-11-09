package ru.yandex.practicum.bank.front.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    public static final String REGISTRATION_ID = "keycloak";

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientRepository authorizedClientRepository) {
        var manager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .build());
        return manager;
    }

    /**
     * Клиент Gateway API: адрес находится через Service Discovery,
     * в запрос добавляется access token вошедшего пользователя.
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder gatewayRestClientBuilder(RestClientBuilderConfigurer configurer,
                                                       OAuth2AuthorizedClientManager authorizedClientManager) {
        var userTokenInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        userTokenInterceptor.setClientRegistrationIdResolver(request -> REGISTRATION_ID);
        return configurer.configure(RestClient.builder())
                .requestInterceptor(userTokenInterceptor);
    }
}
