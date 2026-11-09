package ru.yandex.practicum.bank.notifications.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank(message = "Не указан логин получателя уведомления") String login,
        @NotBlank(message = "Не указан текст уведомления") String message
) {
}
