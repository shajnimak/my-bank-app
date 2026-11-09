package ru.yandex.practicum.bank.transfer.dto;

import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferStatus;

public record TransferResponse(String fromLogin, String toLogin, long amount, TransferStatus status) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(transfer.fromLogin(), transfer.toLogin(), transfer.amount(), transfer.status());
    }
}
