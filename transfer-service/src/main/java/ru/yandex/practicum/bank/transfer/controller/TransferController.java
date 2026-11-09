package ru.yandex.practicum.bank.transfer.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.transfer.dto.TransferRequest;
import ru.yandex.practicum.bank.transfer.dto.TransferResponse;
import ru.yandex.practicum.bank.transfer.security.CurrentLogin;
import ru.yandex.practicum.bank.transfer.service.TransferService;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponse transfer(@CurrentLogin String login, @Valid @RequestBody TransferRequest request) {
        return TransferResponse.from(transferService.transfer(login, request.toLogin(), request.amount()));
    }
}
