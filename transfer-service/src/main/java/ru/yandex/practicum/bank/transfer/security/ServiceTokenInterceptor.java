package ru.yandex.practicum.bank.transfer.security;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;

/**
 * Добавляет в запрос токен сервиса, полученный по Client Credentials Flow.
 */
public class ServiceTokenInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientRegistrationId;

    public ServiceTokenInterceptor(OAuth2AuthorizedClientManager authorizedClientManager, String clientRegistrationId) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistrationId = clientRegistrationId;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal(clientRegistrationId)
                .build());
        if (authorizedClient == null) {
            throw new IllegalStateException("Не удалось получить токен клиента " + clientRegistrationId);
        }
        request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        return execution.execute(request, body);
    }
}
