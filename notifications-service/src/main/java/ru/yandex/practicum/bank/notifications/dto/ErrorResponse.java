package ru.yandex.practicum.bank.notifications.dto;

import java.util.List;

public record ErrorResponse(List<String> errors) {
}
