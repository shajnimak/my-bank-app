package ru.yandex.practicum.bank.cash.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.cash.IntegrationTestBase;
import ru.yandex.practicum.bank.cash.TestJwt;
import ru.yandex.practicum.bank.cash.model.CashAction;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.bank.cash.model.OutboxNotification;
import ru.yandex.practicum.bank.cash.repository.CashOperationRepository;
import ru.yandex.practicum.bank.cash.repository.OutboxNotificationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CashControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CashOperationRepository cashOperationRepository;

    @Autowired
    private OutboxNotificationRepository outboxRepository;

    @Test
    void depositChangesBalanceInAccountsAndEnqueuesNotification() throws Exception {
        mockMvc.perform(post("/api/cash").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"DEPOSIT\", \"amount\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("ivanov"))
                .andExpect(jsonPath("$.action").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.balance").value(150));

        assertThat(cashOperationRepository.findAll())
                .extracting(CashOperation::login, CashOperation::action, CashOperation::amount, CashOperation::balance)
                .containsExactly(tuple("ivanov", CashAction.DEPOSIT, 50L, 150L));
        assertThat(outboxRepository.findAll())
                .extracting(OutboxNotification::login, OutboxNotification::message)
                .containsExactly(tuple("ivanov", "Счёт пополнен на 50 руб, баланс 150 руб"));
    }

    @Test
    void withdrawChangesBalanceInAccounts() throws Exception {
        mockMvc.perform(post("/api/cash").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"WITHDRAW\", \"amount\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("WITHDRAW"))
                .andExpect(jsonPath("$.balance").value(50));

        assertThat(outboxRepository.findAll())
                .extracting(OutboxNotification::message)
                .containsExactly("Со счёта снято 50 руб, баланс 50 руб");
    }

    @Test
    void withdrawMoreThanBalanceIsRejected() throws Exception {
        mockMvc.perform(post("/api/cash").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"WITHDRAW\", \"amount\": 1000000}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Недостаточно средств на счету"));

        assertThat(cashOperationRepository.findAll()).isEmpty();
        assertThat(outboxRepository.findAll()).isEmpty();
    }

    @Test
    void depositToUnknownAccountIsRejected() throws Exception {
        mockMvc.perform(post("/api/cash").with(TestJwt.user("unknown"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"DEPOSIT\", \"amount\": 50}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value("Аккаунт unknown не найден"));
    }

    @Test
    void invalidRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/cash").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"DEPOSIT\", \"amount\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Сумма должна быть больше нуля"));
    }

    @Test
    void cashApiRequiresCashScope() throws Exception {
        mockMvc.perform(post("/api/cash")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_accounts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"DEPOSIT\", \"amount\": 50}"))
                .andExpect(status().isForbidden());
    }
}
