package ru.yandex.practicum.bank.accounts.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BalanceChangeRequest(
        @NotNull(message = "Сумма должна быть указана")
        @Positive(message = "Сумма должна быть больше нуля")
        Long amount
) {
}
