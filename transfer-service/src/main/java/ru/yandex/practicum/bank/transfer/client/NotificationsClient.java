package ru.yandex.practicum.bank.transfer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.transfer.security.ServiceTokenInterceptor;

@Component
public class NotificationsClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public NotificationsClient(RestClient.Builder loadBalancedRestClientBuilder,
                               OAuth2AuthorizedClientManager authorizedClientManager,
                               CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                               @Value("${bank.clients.notifications-url}") String notificationsUrl,
                               @Value("${bank.clients.registration-id}") String registrationId) {
        this.restClient = loadBalancedRestClientBuilder.clone()
                .baseUrl(notificationsUrl)
                .requestInterceptor(new ServiceTokenInterceptor(authorizedClientManager, registrationId))
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("notifications");
    }

    public void send(String login, String message) {
        circuitBreaker.run(() -> restClient.post()
                .uri("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NotificationRequest(login, message))
                .retrieve()
                .toBodilessEntity());
    }

    private record NotificationRequest(String login, String message) {
    }
}
