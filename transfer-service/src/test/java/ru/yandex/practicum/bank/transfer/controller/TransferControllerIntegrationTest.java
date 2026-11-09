package ru.yandex.practicum.bank.transfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.transfer.IntegrationTestBase;
import ru.yandex.practicum.bank.transfer.TestJwt;
import ru.yandex.practicum.bank.transfer.model.OutboxNotification;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferStatus;
import ru.yandex.practicum.bank.transfer.repository.OutboxNotificationRepository;
import ru.yandex.practicum.bank.transfer.repository.TransferRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private OutboxNotificationRepository outboxRepository;

    @Test
    void transferMovesMoneyAndNotifiesBothClients() throws Exception {
        mockMvc.perform(post("/api/transfer").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromLogin").value("ivanov"))
                .andExpect(jsonPath("$.toLogin").value("petrov"))
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        assertThat(transferRepository.findAll())
                .extracting(Transfer::fromLogin, Transfer::toLogin, Transfer::amount, Transfer::status)
                .containsExactly(tuple("ivanov", "petrov", 50L, TransferStatus.COMPLETED));
        assertThat(outboxRepository.findAll())
                .extracting(OutboxNotification::login, OutboxNotification::message)
                .containsExactlyInAnyOrder(
                        tuple("ivanov", "Переведено 50 руб клиенту petrov, баланс 50 руб"),
                        tuple("petrov", "Получено 50 руб от клиента ivanov"));
    }

    @Test
    void transferWithInsufficientFundsIsRejected() throws Exception {
        mockMvc.perform(post("/api/transfer").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 1000000}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Недостаточно средств на счету"));

        assertThat(transferRepository.findAll()).isEmpty();
        assertThat(outboxRepository.findAll()).isEmpty();
    }

    @Test
    void transferToUnknownClientIsCompensated() throws Exception {
        mockMvc.perform(post("/api/transfer").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"unknown\", \"amount\": 50}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value("Аккаунт unknown не найден"));

        assertThat(transferRepository.findAll())
                .extracting(Transfer::toLogin, Transfer::status)
                .containsExactly(tuple("unknown", TransferStatus.FAILED));
        assertThat(outboxRepository.findAll()).isEmpty();
    }

    @Test
    void transferToOwnAccountIsRejected() throws Exception {
        mockMvc.perform(post("/api/transfer").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"ivanov\", \"amount\": 50}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Нельзя перевести деньги на свой счёт"));
    }

    @Test
    void invalidRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/transfer").with(TestJwt.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"\", \"amount\": -5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void transferApiRequiresTransferScope() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cash")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 50}"))
                .andExpect(status().isForbidden());
    }
}
