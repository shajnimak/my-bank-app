package ru.yandex.practicum.bank.cash.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.bank.cash.client.AccountsClient;
import ru.yandex.practicum.bank.cash.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.cash.model.CashAction;
import ru.yandex.practicum.bank.cash.model.CashOperation;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private CashOperationRecorder cashOperationRecorder;

    @InjectMocks
    private CashService cashService;

    @Test
    void depositChangesBalanceAndRecordsOperation() {
        var recorded = new CashOperation(1L, "ivanov", CashAction.DEPOSIT, 50, 150, Instant.now());
        when(accountsClient.deposit("ivanov", 50)).thenReturn(150L);
        when(cashOperationRecorder.record("ivanov", CashAction.DEPOSIT, 50, 150)).thenReturn(recorded);

        assertThat(cashService.process("ivanov", CashAction.DEPOSIT, 50)).isEqualTo(recorded);
    }

    @Test
    void withdrawChangesBalanceAndRecordsOperation() {
        when(accountsClient.withdraw("ivanov", 30)).thenReturn(70L);

        cashService.process("ivanov", CashAction.WITHDRAW, 30);

        verify(cashOperationRecorder).record("ivanov", CashAction.WITHDRAW, 30, 70);
    }

    @Test
    void rejectedOperationIsNotRecorded() {
        when(accountsClient.withdraw("ivanov", 1000)).thenThrow(new AccountOperationRejectedException(
                HttpStatus.CONFLICT, List.of("Недостаточно средств на счету")));

        assertThatThrownBy(() -> cashService.process("ivanov", CashAction.WITHDRAW, 1000))
                .isInstanceOf(AccountOperationRejectedException.class);
        verifyNoInteractions(cashOperationRecorder);
    }
}
