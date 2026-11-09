package ru.yandex.practicum.bank.cash.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.cash.dto.CashOperationRequest;
import ru.yandex.practicum.bank.cash.dto.CashOperationResponse;
import ru.yandex.practicum.bank.cash.security.CurrentLogin;
import ru.yandex.practicum.bank.cash.service.CashService;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping
    public CashOperationResponse process(@CurrentLogin String login, @Valid @RequestBody CashOperationRequest request) {
        return CashOperationResponse.from(cashService.process(login, request.action(), request.amount()));
    }
}
