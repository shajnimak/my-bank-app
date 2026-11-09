package ru.yandex.practicum.bank.transfer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.bank.transfer.client.AccountsClient;
import ru.yandex.practicum.bank.transfer.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.transfer.exception.AccountsUnavailableException;
import ru.yandex.practicum.bank.transfer.exception.SameAccountTransferException;
import ru.yandex.practicum.bank.transfer.model.TransferStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private TransferRecorder transferRecorder;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferWithdrawsFromSenderAndDepositsToRecipient() {
        when(accountsClient.withdraw("ivanov", 30)).thenReturn(70L);

        transferService.transfer("ivanov", "petrov", 30);

        InOrder order = inOrder(accountsClient, transferRecorder);
        order.verify(accountsClient).withdraw("ivanov", 30);
        order.verify(accountsClient).deposit("petrov", 30);
        order.verify(transferRecorder).recordCompleted("ivanov", "petrov", 30, 70);
    }

    @Test
    void transferToOwnAccountIsRejectedWithoutCallingAccounts() {
        assertThatThrownBy(() -> transferService.transfer("ivanov", "ivanov", 30))
                .isInstanceOf(SameAccountTransferException.class);
        verifyNoInteractions(accountsClient, transferRecorder);
    }

    @Test
    void failedWithdrawalStopsTransfer() {
        when(accountsClient.withdraw("ivanov", 1000)).thenThrow(new AccountOperationRejectedException(
                HttpStatus.CONFLICT, List.of("Недостаточно средств на счету")));

        assertThatThrownBy(() -> transferService.transfer("ivanov", "petrov", 1000))
                .isInstanceOf(AccountOperationRejectedException.class);
        verify(accountsClient, never()).deposit(anyString(), anyLong());
        verifyNoInteractions(transferRecorder);
    }

    @Test
    void failedDepositIsCompensatedBySenderRefund() {
        when(accountsClient.withdraw("ivanov", 30)).thenReturn(70L);
        when(accountsClient.deposit("unknown", 30)).thenThrow(new AccountOperationRejectedException(
                HttpStatus.NOT_FOUND, List.of("Аккаунт unknown не найден")));

        assertThatThrownBy(() -> transferService.transfer("ivanov", "unknown", 30))
                .isInstanceOf(AccountOperationRejectedException.class);
        verify(accountsClient).deposit("ivanov", 30);
        verify(transferRecorder).recordFailed("ivanov", "unknown", 30, TransferStatus.FAILED);
    }

    @Test
    void failedCompensationIsRecorded() {
        when(accountsClient.withdraw("ivanov", 30)).thenReturn(70L);
        when(accountsClient.deposit("petrov", 30)).thenThrow(new AccountsUnavailableException(new RuntimeException()));
        when(accountsClient.deposit("ivanov", 30)).thenThrow(new AccountsUnavailableException(new RuntimeException()));

        assertThatThrownBy(() -> transferService.transfer("ivanov", "petrov", 30))
                .isInstanceOf(AccountsUnavailableException.class);
        verify(transferRecorder).recordFailed("ivanov", "petrov", 30, TransferStatus.COMPENSATION_FAILED);
    }
}
