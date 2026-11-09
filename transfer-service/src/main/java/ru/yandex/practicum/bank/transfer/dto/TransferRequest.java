package ru.yandex.practicum.bank.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotBlank(message = "Не выбран получатель перевода")
        String toLogin,
        @NotNull(message = "Сумма должна быть указана")
        @Positive(message = "Сумма должна быть больше нуля")
        Long amount
) {
}
