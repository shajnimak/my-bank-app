package ru.yandex.practicum.bank.front.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.front.client.AccountsClient;

import java.time.LocalDate;

@Service
public class AccountService {

    private final AccountsClient accountsClient;

    public AccountService(AccountsClient accountsClient) {
        this.accountsClient = accountsClient;
    }

    public String updateAccount(String name, LocalDate birthdate) {
        accountsClient.updateCurrentAccount(name, birthdate);
        return "Данные аккаунта сохранены";
    }
}
