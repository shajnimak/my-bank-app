package ru.yandex.practicum.bank.front.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.front.controller.dto.AccountDto;

import java.time.Instant;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.yandex.practicum.bank.front.TestUsers.user;

/**
 * Фронт против заглушек сервисов, сгенерированных из их контрактов.
 */
@SpringBootTest(properties = {
        "bank.clients.accounts-url=http://accounts-service",
        "bank.clients.cash-url=http://cash-service",
        "bank.clients.transfer-url=http://transfer-service"
})
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
        ids = {
                "ru.yandex.practicum:accounts-service",
                "ru.yandex.practicum:cash-service",
                "ru.yandex.practicum:transfer-service"
        },
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
class MainControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @BeforeEach
    void setUpUserToken() {
        var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "user-token",
                Instant.now(), Instant.now().plusSeconds(300));
        when(authorizedClientManager.authorize(any())).thenReturn(new OAuth2AuthorizedClient(
                clientRegistrationRepository.findByRegistrationId("keycloak"), "ivanov", token));
    }

    @Test
    void accountPageShowsDataFromAccountsService() throws Exception {
        mockMvc.perform(get("/account").with(user()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("login", "ivanov"))
                .andExpect(model().attribute("name", "Иванов Иван"))
                .andExpect(model().attribute("birthdate", "2001-01-01"))
                .andExpect(model().attribute("sum", 100L))
                .andExpect(model().attribute("accounts", contains(
                        new AccountDto("petrov", "Петров Петр"),
                        new AccountDto("sidorov", "Сидоров Сидор"))))
                .andExpect(content().string(containsString("Иванов Иван")));
    }

    @Test
    void editAccountSavesDataInAccountsService() throws Exception {
        mockMvc.perform(post("/account").with(user()).with(csrf())
                        .param("name", "Иванов Пётр")
                        .param("birthdate", "1990-02-03"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Данные аккаунта сохранены"))
                .andExpect(model().attribute("errors", (Object) null));
    }

    @Test
    void editAccountShowsValidationErrorOfAccountsService() throws Exception {
        mockMvc.perform(post("/account").with(user()).with(csrf())
                        .param("name", "Иванов Иван")
                        .param("birthdate", "2015-01-01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Возраст должен быть старше 18 лет")))
                .andExpect(model().attribute("info", (Object) null));
    }

    @Test
    void depositShowsSuccessMessage() throws Exception {
        mockMvc.perform(post("/cash").with(user()).with(csrf())
                        .param("value", "100")
                        .param("action", "PUT"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Положено 100 руб"));
    }

    @Test
    void withdrawShowsSuccessMessage() throws Exception {
        mockMvc.perform(post("/cash").with(user()).with(csrf())
                        .param("value", "100")
                        .param("action", "GET"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Снято 100 руб"));
    }

    @Test
    void withdrawMoreThanBalanceShowsError() throws Exception {
        mockMvc.perform(post("/cash").with(user()).with(csrf())
                        .param("value", "1000000")
                        .param("action", "GET"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Недостаточно средств на счету")))
                .andExpect(content().string(containsString("Недостаточно средств на счету")));
    }

    @Test
    void transferShowsRecipientName() throws Exception {
        mockMvc.perform(post("/transfer").with(user()).with(csrf())
                        .param("value", "100")
                        .param("login", "petrov"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Успешно переведено 100 руб клиенту Петров Петр"));
    }

    @Test
    void transferMoreThanBalanceShowsError() throws Exception {
        mockMvc.perform(post("/transfer").with(user()).with(csrf())
                        .param("value", "1000000")
                        .param("login", "petrov"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Недостаточно средств на счету")));
    }

    @Test
    void anonymousUserIsRedirectedToAuthorizationServer() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/keycloak"));
    }
}
