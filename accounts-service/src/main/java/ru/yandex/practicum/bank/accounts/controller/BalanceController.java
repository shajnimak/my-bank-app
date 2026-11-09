package ru.yandex.practicum.bank.accounts.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.accounts.dto.BalanceChangeRequest;
import ru.yandex.practicum.bank.accounts.dto.BalanceResponse;
import ru.yandex.practicum.bank.accounts.service.AccountService;

@RestController
@RequestMapping("/api/accounts/{login}")
public class BalanceController {

    private final AccountService accountService;

    public BalanceController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/deposit")
    public BalanceResponse deposit(@PathVariable String login, @Valid @RequestBody BalanceChangeRequest request) {
        return BalanceResponse.from(accountService.deposit(login, request.amount()));
    }

    @PostMapping("/withdraw")
    public BalanceResponse withdraw(@PathVariable String login, @Valid @RequestBody BalanceChangeRequest request) {
        return BalanceResponse.from(accountService.withdraw(login, request.amount()));
    }
}
