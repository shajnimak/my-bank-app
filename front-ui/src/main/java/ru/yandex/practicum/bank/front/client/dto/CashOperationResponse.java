package ru.yandex.practicum.bank.front.client.dto;

public record CashOperationResponse(String login, CashOperationRequest.CashOperationType action, long amount,
                                    long balance) {
}
