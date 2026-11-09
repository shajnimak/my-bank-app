package ru.yandex.practicum.bank.notifications.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("notifications")
public record Notification(
        @Id Long id,
        String login,
        String message,
        Instant createdAt
) {

    public static Notification create(String login, String message, Instant createdAt) {
        return new Notification(null, login, message, createdAt);
    }
}
