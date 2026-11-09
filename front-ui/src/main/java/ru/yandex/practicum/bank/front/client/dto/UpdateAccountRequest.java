package ru.yandex.practicum.bank.front.client.dto;

import java.time.LocalDate;

public record UpdateAccountRequest(String name, LocalDate birthdate) {
}
