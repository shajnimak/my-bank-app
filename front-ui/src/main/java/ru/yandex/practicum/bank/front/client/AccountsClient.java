package ru.yandex.practicum.bank.front.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.front.client.dto.AccountResponse;
import ru.yandex.practicum.bank.front.client.dto.AccountSummaryResponse;
import ru.yandex.practicum.bank.front.client.dto.UpdateAccountRequest;

import java.time.LocalDate;
import java.util.List;

@Component
public class AccountsClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public AccountsClient(RestClient.Builder gatewayRestClientBuilder,
                          CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                          @Value("${bank.clients.accounts-url}") String accountsUrl) {
        this.restClient = gatewayRestClientBuilder.clone().baseUrl(accountsUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("accounts");
    }

    public AccountResponse getCurrentAccount() {
        return circuitBreaker.run(
                () -> restClient.get()
                        .uri("/api/accounts/me")
                        .retrieve()
                        .body(AccountResponse.class),
                AccountsClient::fail);
    }

    public AccountResponse updateCurrentAccount(String name, LocalDate birthdate) {
        return circuitBreaker.run(
                () -> restClient.put()
                        .uri("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new UpdateAccountRequest(name, birthdate))
                        .retrieve()
                        .body(AccountResponse.class),
                AccountsClient::fail);
    }

    public List<AccountSummaryResponse> getOtherAccounts() {
        return circuitBreaker.run(
                () -> restClient.get()
                        .uri("/api/accounts")
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<AccountSummaryResponse>>() {
                        }),
                AccountsClient::fail);
    }

    private static <T> T fail(Throwable throwable) {
        throw ApiErrors.translate(throwable);
    }
}
