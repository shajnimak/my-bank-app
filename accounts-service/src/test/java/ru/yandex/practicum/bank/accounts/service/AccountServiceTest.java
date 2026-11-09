package ru.yandex.practicum.bank.accounts.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.accounts.exception.AccountNotFoundException;
import ru.yandex.practicum.bank.accounts.exception.InsufficientFundsException;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.outbox.NotificationOutbox;
import ru.yandex.practicum.bank.accounts.repository.AccountRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private NotificationOutbox notificationOutbox;

    @InjectMocks
    private AccountService accountService;

    @Test
    void updateProfileEnqueuesNotification() {
        when(accountRepository.updateProfile("ivanov", "Иванов Иван", BIRTHDATE)).thenReturn(true);
        when(accountRepository.findByLogin("ivanov"))
                .thenReturn(Optional.of(new Account(1L, "ivanov", "Иванов Иван", BIRTHDATE, 100)));

        Account account = accountService.updateProfile("ivanov", "Иванов Иван", BIRTHDATE);

        assertThat(account.name()).isEqualTo("Иванов Иван");
        verify(notificationOutbox).enqueue("ivanov", "Данные аккаунта изменены: Иванов Иван, дата рождения 01.01.2001");
    }

    @Test
    void updateProfileOfUnknownAccountFails() {
        when(accountRepository.updateProfile("unknown", "Имя", BIRTHDATE)).thenReturn(false);

        assertThatThrownBy(() -> accountService.updateProfile("unknown", "Имя", BIRTHDATE))
                .isInstanceOf(AccountNotFoundException.class);
        verify(notificationOutbox, never()).enqueue(anyString(), anyString());
    }

    @Test
    void depositToUnknownAccountFails() {
        when(accountRepository.deposit("unknown", 10)).thenReturn(false);

        assertThatThrownBy(() -> accountService.deposit("unknown", 10))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Аккаунт unknown не найден");
    }

    @Test
    void withdrawMoreThanBalanceFails() {
        when(accountRepository.withdraw("ivanov", 1000)).thenReturn(false);
        when(accountRepository.findByLogin("ivanov"))
                .thenReturn(Optional.of(new Account(1L, "ivanov", "Иванов Иван", BIRTHDATE, 100)));

        assertThatThrownBy(() -> accountService.withdraw("ivanov", 1000))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Недостаточно средств на счету");
    }

    @Test
    void withdrawFromUnknownAccountFails() {
        when(accountRepository.withdraw("unknown", 10)).thenReturn(false);
        when(accountRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.withdraw("unknown", 10))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void withdrawReturnsUpdatedAccount() {
        when(accountRepository.withdraw("ivanov", 40)).thenReturn(true);
        when(accountRepository.findByLogin("ivanov"))
                .thenReturn(Optional.of(new Account(1L, "ivanov", "Иванов Иван", BIRTHDATE, 60)));

        assertThat(accountService.withdraw("ivanov", 40).balance()).isEqualTo(60);
    }
}
