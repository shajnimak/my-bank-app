package ru.yandex.practicum.bank.cash.dto;

import ru.yandex.practicum.bank.cash.model.CashAction;
import ru.yandex.practicum.bank.cash.model.CashOperation;

public record CashOperationResponse(String login, CashAction action, long amount, long balance) {

    public static CashOperationResponse from(CashOperation operation) {
        return new CashOperationResponse(operation.login(), operation.action(), operation.amount(), operation.balance());
    }
}
