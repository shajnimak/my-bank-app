package ru.yandex.practicum.bank.accounts.dto;

import ru.yandex.practicum.bank.accounts.model.Account;

public record AccountSummaryResponse(String login, String name) {

    public static AccountSummaryResponse from(Account account) {
        return new AccountSummaryResponse(account.login(), account.name());
    }
}
