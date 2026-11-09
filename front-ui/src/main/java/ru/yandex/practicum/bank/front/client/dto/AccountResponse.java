package ru.yandex.practicum.bank.front.client.dto;

import java.time.LocalDate;

public record AccountResponse(String login, String name, LocalDate birthdate, long balance) {
}
