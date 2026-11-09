package ru.yandex.practicum.bank.transfer.dto;

import java.util.List;

public record ErrorResponse(List<String> errors) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(List.of(error));
    }
}
