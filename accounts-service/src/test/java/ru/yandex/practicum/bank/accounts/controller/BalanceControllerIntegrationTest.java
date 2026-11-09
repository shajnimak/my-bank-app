package ru.yandex.practicum.bank.accounts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.accounts.IntegrationTestBase;
import ru.yandex.practicum.bank.accounts.TestJwt;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.repository.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BalanceControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void depositIncreasesBalance() throws Exception {
        mockMvc.perform(post("/api/accounts/petrov/deposit").with(TestJwt.service())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("petrov"))
                .andExpect(jsonPath("$.balance").value(150));

        assertThat(balanceOf("petrov")).isEqualTo(150);
    }

    @Test
    void withdrawDecreasesBalance() throws Exception {
        mockMvc.perform(post("/api/accounts/petrov/withdraw").with(TestJwt.service())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));

        assertThat(balanceOf("petrov")).isZero();
    }

    @Test
    void withdrawRejectsAmountGreaterThanBalance() throws Exception {
        mockMvc.perform(post("/api/accounts/petrov/withdraw").with(TestJwt.service())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 101}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Недостаточно средств на счету"));

        assertThat(balanceOf("petrov")).isEqualTo(100);
    }

    @Test
    void depositToUnknownAccountReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/accounts/unknown/deposit").with(TestJwt.service())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void balanceChangeRejectsNonPositiveAmount() throws Exception {
        mockMvc.perform(post("/api/accounts/petrov/deposit").with(TestJwt.service())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Сумма должна быть больше нуля"));
    }

    @Test
    void balanceChangeIsForbiddenForUserToken() throws Exception {
        mockMvc.perform(post("/api/accounts/ivanov/deposit").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000}"))
                .andExpect(status().isForbidden());

        assertThat(balanceOf("ivanov")).isEqualTo(100);
    }

    private long balanceOf(String login) {
        return accountRepository.findByLogin(login).map(Account::balance).orElseThrow();
    }
}
