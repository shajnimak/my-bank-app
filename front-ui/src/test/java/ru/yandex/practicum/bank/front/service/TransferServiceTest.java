package ru.yandex.practicum.bank.front.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.front.client.AccountsClient;
import ru.yandex.practicum.bank.front.client.BankApiException;
import ru.yandex.practicum.bank.front.client.TransferClient;
import ru.yandex.practicum.bank.front.client.dto.AccountSummaryResponse;
import ru.yandex.practicum.bank.front.client.dto.TransferRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferClient transferClient;

    @Mock
    private AccountsClient accountsClient;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferReturnsMessageWithRecipientName() {
        when(accountsClient.getOtherAccounts()).thenReturn(List.of(
                new AccountSummaryResponse("petrov", "Петров Петр"),
                new AccountSummaryResponse("sidorov", "Сидоров Сидор")));

        assertThat(transferService.transfer("sidorov", 25L))
                .isEqualTo("Успешно переведено 25 руб клиенту Сидоров Сидор");
    }

    @Test
    void failedTransferIsNotReported() {
        when(transferClient.transfer(new TransferRequest("petrov", 1000L)))
                .thenThrow(new BankApiException(List.of("Недостаточно средств на счету")));

        assertThatThrownBy(() -> transferService.transfer("petrov", 1000L))
                .isInstanceOf(BankApiException.class);
        verifyNoInteractions(accountsClient);
    }
}
