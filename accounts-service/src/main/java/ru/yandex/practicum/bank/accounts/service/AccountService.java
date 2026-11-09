package ru.yandex.practicum.bank.accounts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.accounts.exception.AccountNotFoundException;
import ru.yandex.practicum.bank.accounts.exception.InsufficientFundsException;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.outbox.NotificationOutbox;
import ru.yandex.practicum.bank.accounts.repository.AccountRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AccountService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AccountRepository accountRepository;
    private final NotificationOutbox notificationOutbox;

    public AccountService(AccountRepository accountRepository, NotificationOutbox notificationOutbox) {
        this.accountRepository = accountRepository;
        this.notificationOutbox = notificationOutbox;
    }

    @Transactional(readOnly = true)
    public Account getAccount(String login) {
        return accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
    }

    @Transactional(readOnly = true)
    public List<Account> getOtherAccounts(String login) {
        return accountRepository.findByLoginNotOrderByName(login);
    }

    @Transactional
    public Account updateProfile(String login, String name, LocalDate birthdate) {
        if (!accountRepository.updateProfile(login, name, birthdate)) {
            throw new AccountNotFoundException(login);
        }
        notificationOutbox.enqueue(login, "Данные аккаунта изменены: %s, дата рождения %s"
                .formatted(name, birthdate.format(DATE_FORMAT)));
        return getAccount(login);
    }

    @Transactional
    public Account deposit(String login, long amount) {
        if (!accountRepository.deposit(login, amount)) {
            throw new AccountNotFoundException(login);
        }
        return getAccount(login);
    }

    @Transactional
    public Account withdraw(String login, long amount) {
        if (!accountRepository.withdraw(login, amount)) {
            getAccount(login);
            throw new InsufficientFundsException();
        }
        return getAccount(login);
    }
}
