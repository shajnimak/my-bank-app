package ru.yandex.practicum.bank.cash.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.cash.dto.ErrorResponse;
import ru.yandex.practicum.bank.cash.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.cash.exception.AccountsUnavailableException;
import ru.yandex.practicum.bank.cash.security.ServiceTokenInterceptor;

import java.util.List;

@Component
public class AccountsClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public AccountsClient(RestClient.Builder loadBalancedRestClientBuilder,
                          OAuth2AuthorizedClientManager authorizedClientManager,
                          CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                          @Value("${bank.clients.accounts-url}") String accountsUrl,
                          @Value("${bank.clients.registration-id}") String registrationId) {
        this.restClient = loadBalancedRestClientBuilder.clone()
                .baseUrl(accountsUrl)
                .requestInterceptor(new ServiceTokenInterceptor(authorizedClientManager, registrationId))
                .build();
        this.circuitBreaker = circuitBreakerFactory.create("accounts");
    }

    public long deposit(String login, long amount) {
        return changeBalance(login, "deposit", amount);
    }

    public long withdraw(String login, long amount) {
        return changeBalance(login, "withdraw", amount);
    }

    private long changeBalance(String login, String operation, long amount) {
        BalanceResponse response = circuitBreaker.run(
                () -> restClient.post()
                        .uri("/api/accounts/{login}/{operation}", login, operation)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new BalanceChangeRequest(amount))
                        .retrieve()
                        .body(BalanceResponse.class),
                AccountsClient::translateError);
        return response.balance();
    }

    private static BalanceResponse translateError(Throwable throwable) {
        if (throwable instanceof HttpClientErrorException e) {
            throw new AccountOperationRejectedException(e.getStatusCode(), errorsOf(e));
        }
        throw new AccountsUnavailableException(throwable);
    }

    private static List<String> errorsOf(HttpClientErrorException e) {
        try {
            ErrorResponse body = e.getResponseBodyAs(ErrorResponse.class);
            if (body != null && body.errors() != null && !body.errors().isEmpty()) {
                return body.errors();
            }
        } catch (RuntimeException ignored) {
            // тело ответа не в формате ErrorResponse
        }
        return List.of("Операция со счётом отклонена");
    }

    private record BalanceChangeRequest(long amount) {
    }

    private record BalanceResponse(String login, long balance) {
    }
}
