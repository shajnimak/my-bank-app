package ru.yandex.practicum.bank.front.client.dto;

public record TransferResponse(String fromLogin, String toLogin, long amount, String status) {
}
