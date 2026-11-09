package ru.yandex.practicum.bank.transfer;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "bank.outbox.scheduling-enabled=false")
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
        ids = {"ru.yandex.practicum:notifications-service", "ru.yandex.practicum:accounts-service"},
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
@Import(TestcontainersConfiguration.class)
@Sql("/sql/reset-transfer.sql")
public abstract class IntegrationTestBase {

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    protected OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUpServiceToken() {
        var registration = clientRegistrationRepository.findByRegistrationId("transfer-service");
        var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "service-token",
                Instant.now(), Instant.now().plusSeconds(300));
        when(authorizedClientManager.authorize(any()))
                .thenReturn(new OAuth2AuthorizedClient(registration, "transfer-service", token));
    }
}
