package ru.yandex.practicum.bank.accounts.dto;

import ru.yandex.practicum.bank.accounts.model.Account;

public record BalanceResponse(String login, long balance) {

    public static BalanceResponse from(Account account) {
        return new BalanceResponse(account.login(), account.balance());
    }
}
