package ru.yandex.practicum.bank.accounts.dto;

import ru.yandex.practicum.bank.accounts.model.Account;

import java.time.LocalDate;

public record AccountResponse(String login, String name, LocalDate birthdate, long balance) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.login(), account.name(), account.birthdate(), account.balance());
    }
}
