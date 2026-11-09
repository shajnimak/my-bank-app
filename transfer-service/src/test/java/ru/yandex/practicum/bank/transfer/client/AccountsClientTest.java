package ru.yandex.practicum.bank.transfer.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.transfer.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.transfer.exception.AccountsUnavailableException;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AccountsClientTest {

    private static final CircuitBreaker PASS_THROUGH = new CircuitBreaker() {
        @Override
        public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
            try {
                return toRun.get();
            } catch (Throwable e) {
                return fallback.apply(e);
            }
        }
    };

    private MockRestServiceServer server;
    private AccountsClient accountsClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        var registration = ClientRegistration.withRegistrationId("transfer-service")
                .clientId("transfer-service")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("http://keycloak/token")
                .build();
        var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "service-token",
                Instant.now(), Instant.now().plusSeconds(60));
        OAuth2AuthorizedClientManager authorizedClientManager = mock(OAuth2AuthorizedClientManager.class);
        when(authorizedClientManager.authorize(any()))
                .thenReturn(new OAuth2AuthorizedClient(registration, "transfer-service", token));
        CircuitBreakerFactory<?, ?> circuitBreakerFactory = mock(CircuitBreakerFactory.class);
        when(circuitBreakerFactory.create("accounts")).thenReturn(PASS_THROUGH);

        accountsClient = new AccountsClient(builder, authorizedClientManager, circuitBreakerFactory,
                "http://accounts-service", "transfer-service");
    }

    @Test
    void depositSendsServiceTokenAndReturnsBalance() {
        server.expect(requestTo("http://accounts-service/api/accounts/ivanov/deposit"))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andExpect(content().json("{\"amount\": 50}"))
                .andRespond(withSuccess("{\"login\": \"ivanov\", \"balance\": 150}", MediaType.APPLICATION_JSON));

        assertThat(accountsClient.deposit("ivanov", 50)).isEqualTo(150);
        server.verify();
    }

    @Test
    void clientErrorIsTranslatedToRejectedOperation() {
        server.expect(requestTo("http://accounts-service/api/accounts/ivanov/withdraw"))
                .andRespond(withStatus(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"errors\": [\"Недостаточно средств на счету\"]}"));

        assertThatThrownBy(() -> accountsClient.withdraw("ivanov", 1000))
                .isInstanceOfSatisfying(AccountOperationRejectedException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getErrors()).containsExactly("Недостаточно средств на счету");
                });
    }

    @Test
    void serverErrorIsTranslatedToUnavailableAccounts() {
        server.expect(requestTo("http://accounts-service/api/accounts/ivanov/withdraw"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> accountsClient.withdraw("ivanov", 10))
                .isInstanceOf(AccountsUnavailableException.class)
                .hasMessage("Сервис аккаунтов временно недоступен, попробуйте позже");
    }
}
