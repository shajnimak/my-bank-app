package ru.yandex.practicum.bank.notifications.dto;

import ru.yandex.practicum.bank.notifications.model.Notification;

import java.time.Instant;

public record NotificationResponse(Long id, String login, String message, Instant createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(), notification.login(), notification.message(), notification.createdAt());
    }
}
