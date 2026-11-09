package ru.yandex.practicum.bank.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureStubRunner(
        ids = {
                "ru.yandex.practicum:accounts-service",
                "ru.yandex.practicum:cash-service",
                "ru.yandex.practicum:transfer-service"
        },
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
class GatewayRoutingTest {

    private static final String TOKEN = "user-token";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    void setUpJwtDecoder() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(Jwt.withTokenValue(TOKEN)
                .header("alg", "RS256")
                .claim("preferred_username", "ivanov")
                .claim("scope", "accounts cash transfer")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build()));
    }

    @Test
    void routesCurrentAccountRequestToAccountsService() {
        webTestClient.get().uri("/api/accounts/me")
                .headers(headers -> headers.setBearerAuth(TOKEN))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo("ivanov")
                .jsonPath("$.balance").isEqualTo(100);
    }

    @Test
    void routesOtherAccountsRequestToAccountsService() {
        webTestClient.get().uri("/api/accounts")
                .headers(headers -> headers.setBearerAuth(TOKEN))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].login").isEqualTo("petrov");
    }

    @Test
    void routesCashRequestToCashService() {
        webTestClient.post().uri("/api/cash")
                .headers(headers -> headers.setBearerAuth(TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"action\": \"DEPOSIT\", \"amount\": 100}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(200);
    }

    @Test
    void routesTransferRequestToTransferService() {
        webTestClient.post().uri("/api/transfer")
                .headers(headers -> headers.setBearerAuth(TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"toLogin\": \"petrov\", \"amount\": 100}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void doesNotExposeInternalBalanceApi() {
        webTestClient.post().uri("/api/accounts/ivanov/deposit")
                .headers(headers -> headers.setBearerAuth(TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 100}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void rejectsRequestWithoutToken() {
        webTestClient.get().uri("/api/accounts/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
