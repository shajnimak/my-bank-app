package ru.yandex.practicum.bank.accounts.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("accounts")
public record Account(
        @Id Long id,
        String login,
        String name,
        LocalDate birthdate,
        long balance
) {
}
