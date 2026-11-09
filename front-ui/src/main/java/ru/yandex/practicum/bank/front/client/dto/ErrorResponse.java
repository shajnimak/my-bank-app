package ru.yandex.practicum.bank.front.client.dto;

import java.util.List;

public record ErrorResponse(List<String> errors) {
}
