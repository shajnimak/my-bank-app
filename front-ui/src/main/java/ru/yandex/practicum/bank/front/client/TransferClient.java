package ru.yandex.practicum.bank.front.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.front.client.dto.TransferRequest;
import ru.yandex.practicum.bank.front.client.dto.TransferResponse;

@Component
public class TransferClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public TransferClient(RestClient.Builder gatewayRestClientBuilder,
                          CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                          @Value("${bank.clients.transfer-url}") String transferUrl) {
        this.restClient = gatewayRestClientBuilder.clone().baseUrl(transferUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("transfer");
    }

    public TransferResponse transfer(TransferRequest request) {
        return circuitBreaker.run(
                () -> restClient.post()
                        .uri("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(TransferResponse.class),
                throwable -> {
                    throw ApiErrors.translate(throwable);
                });
    }
}
