package ru.yandex.practicum.bank.front.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.front.client.AccountsClient;
import ru.yandex.practicum.bank.front.client.dto.AccountResponse;
import ru.yandex.practicum.bank.front.controller.dto.AccountDto;

import java.util.List;

/**
 * Собирает данные главной страницы из ответов сервиса аккаунтов (UI Composition).
 */
@Service
public class MainPageService {

    private final AccountsClient accountsClient;

    public MainPageService(AccountsClient accountsClient) {
        this.accountsClient = accountsClient;
    }

    public MainPage load() {
        AccountResponse account = accountsClient.getCurrentAccount();
        List<AccountDto> accounts = accountsClient.getOtherAccounts().stream()
                .map(other -> new AccountDto(other.login(), other.name()))
                .toList();
        return new MainPage(account.name(), account.birthdate(), account.balance(), accounts);
    }
}
