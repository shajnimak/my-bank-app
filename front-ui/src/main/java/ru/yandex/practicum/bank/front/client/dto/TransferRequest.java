package ru.yandex.practicum.bank.front.client.dto;

public record TransferRequest(String toLogin, Long amount) {
}
