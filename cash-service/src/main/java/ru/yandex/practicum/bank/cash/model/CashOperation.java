package ru.yandex.practicum.bank.cash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("cash_operations")
public record CashOperation(
        @Id Long id,
        String login,
        CashAction action,
        long amount,
        long balance,
        Instant createdAt
) {

    public static CashOperation create(String login, CashAction action, long amount, long balance, Instant createdAt) {
        return new CashOperation(null, login, action, amount, balance, createdAt);
    }
}
