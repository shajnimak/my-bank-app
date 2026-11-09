package ru.yandex.practicum.bank.front.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.front.client.dto.CashOperationRequest;
import ru.yandex.practicum.bank.front.client.dto.CashOperationResponse;

@Component
public class CashClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public CashClient(RestClient.Builder gatewayRestClientBuilder,
                      CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                      @Value("${bank.clients.cash-url}") String cashUrl) {
        this.restClient = gatewayRestClientBuilder.clone().baseUrl(cashUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("cash");
    }

    public CashOperationResponse process(CashOperationRequest request) {
        return circuitBreaker.run(
                () -> restClient.post()
                        .uri("/api/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(CashOperationResponse.class),
                throwable -> {
                    throw ApiErrors.translate(throwable);
                });
    }
}
