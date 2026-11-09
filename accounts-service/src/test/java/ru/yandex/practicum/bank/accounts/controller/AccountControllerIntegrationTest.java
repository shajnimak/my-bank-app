package ru.yandex.practicum.bank.accounts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.accounts.IntegrationTestBase;
import ru.yandex.practicum.bank.accounts.TestJwt;
import ru.yandex.practicum.bank.accounts.model.OutboxNotification;
import ru.yandex.practicum.bank.accounts.repository.AccountRepository;
import ru.yandex.practicum.bank.accounts.repository.OutboxNotificationRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OutboxNotificationRepository outboxRepository;

    @Test
    void getCurrentAccountReturnsAccountOfTokenOwner() throws Exception {
        mockMvc.perform(get("/api/accounts/me").with(TestJwt.user("ivanov")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("ivanov"))
                .andExpect(jsonPath("$.name").value("Иванов Иван"))
                .andExpect(jsonPath("$.birthdate").value("2001-01-01"))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void getCurrentAccountReturnsNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/accounts/me").with(TestJwt.user("unknown")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value("Аккаунт unknown не найден"));
    }

    @Test
    void updateCurrentAccountSavesProfileAndEnqueuesNotification() throws Exception {
        mockMvc.perform(put("/api/accounts/me").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Иванов Пётр", "birthdate": "1990-02-03"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иванов Пётр"))
                .andExpect(jsonPath("$.birthdate").value("1990-02-03"))
                .andExpect(jsonPath("$.balance").value(100));

        assertThat(accountRepository.findByLogin("ivanov")).get()
                .satisfies(account -> {
                    assertThat(account.name()).isEqualTo("Иванов Пётр");
                    assertThat(account.birthdate()).isEqualTo(LocalDate.of(1990, 2, 3));
                });
        assertThat(outboxRepository.findAll())
                .extracting(OutboxNotification::login, OutboxNotification::message)
                .containsExactly(tuple(
                        "ivanov", "Данные аккаунта изменены: Иванов Пётр, дата рождения 03.02.1990"));
    }

    @Test
    void updateCurrentAccountRejectsInvalidData() throws Exception {
        mockMvc.perform(put("/api/accounts/me").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "birthdate": "%s"}
                                """.formatted(LocalDate.now().minusYears(17))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", containsInAnyOrder(
                        "Возраст должен быть старше 18 лет",
                        "Фамилия и имя должны быть заполнены")));

        assertThat(accountRepository.findByLogin("ivanov")).get()
                .extracting(account -> account.name())
                .isEqualTo("Иванов Иван");
        assertThat(outboxRepository.findAll()).isEmpty();
    }

    @Test
    void getOtherAccountsExcludesCurrentUser() throws Exception {
        mockMvc.perform(get("/api/accounts").with(TestJwt.user("ivanov")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].login").value("petrov"))
                .andExpect(jsonPath("$[0].name").value("Петров Петр"))
                .andExpect(jsonPath("$[1].login").value("sidorov"));
    }

    @Test
    void accountsApiRequiresAccountsScope() throws Exception {
        mockMvc.perform(get("/api/accounts/me").with(TestJwt.service()))
                .andExpect(status().isForbidden());
    }

    @Test
    void accountsApiRequiresToken() throws Exception {
        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());
    }
}
