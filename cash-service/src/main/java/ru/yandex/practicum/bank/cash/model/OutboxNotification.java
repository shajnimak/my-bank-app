package ru.yandex.practicum.bank.cash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("outbox_notifications")
public record OutboxNotification(
        @Id Long id,
        String login,
        String message,
        Instant createdAt,
        Instant sentAt
) {

    public static OutboxNotification create(String login, String message, Instant createdAt) {
        return new OutboxNotification(null, login, message, createdAt, null);
    }
}
