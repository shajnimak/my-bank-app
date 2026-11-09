package ru.yandex.practicum.bank.transfer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("transfers")
public record Transfer(
        @Id Long id,
        String fromLogin,
        String toLogin,
        long amount,
        TransferStatus status,
        Instant createdAt
) {

    public static Transfer create(String fromLogin, String toLogin, long amount, TransferStatus status,
                                  Instant createdAt) {
        return new Transfer(null, fromLogin, toLogin, amount, status, createdAt);
    }
}
