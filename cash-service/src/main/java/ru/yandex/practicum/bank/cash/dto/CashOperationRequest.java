package ru.yandex.practicum.bank.cash.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.bank.cash.model.CashAction;

public record CashOperationRequest(
        @NotNull(message = "Не указано действие с наличными")
        CashAction action,
        @NotNull(message = "Сумма должна быть указана")
        @Positive(message = "Сумма должна быть больше нуля")
        Long amount
) {
}
