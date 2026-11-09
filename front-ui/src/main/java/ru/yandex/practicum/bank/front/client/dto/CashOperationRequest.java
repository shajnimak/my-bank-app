package ru.yandex.practicum.bank.front.client.dto;

public record CashOperationRequest(CashOperationType action, Long amount) {

    public enum CashOperationType {
        DEPOSIT,
        WITHDRAW
    }
}
