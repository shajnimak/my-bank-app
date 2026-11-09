package ru.yandex.practicum.bank.front.service;

import ru.yandex.practicum.bank.front.controller.dto.AccountDto;

import java.time.LocalDate;
import java.util.List;

public record MainPage(String name, LocalDate birthdate, long sum, List<AccountDto> accounts) {
}
