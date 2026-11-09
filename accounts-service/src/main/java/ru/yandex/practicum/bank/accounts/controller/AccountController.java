package ru.yandex.practicum.bank.accounts.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.accounts.dto.AccountResponse;
import ru.yandex.practicum.bank.accounts.dto.AccountSummaryResponse;
import ru.yandex.practicum.bank.accounts.dto.UpdateAccountRequest;
import ru.yandex.practicum.bank.accounts.security.CurrentLogin;
import ru.yandex.practicum.bank.accounts.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public AccountResponse getCurrentAccount(@CurrentLogin String login) {
        return AccountResponse.from(accountService.getAccount(login));
    }

    @PutMapping("/me")
    public AccountResponse updateCurrentAccount(@CurrentLogin String login,
                                                @Valid @RequestBody UpdateAccountRequest request) {
        return AccountResponse.from(accountService.updateProfile(login, request.name().trim(), request.birthdate()));
    }

    @GetMapping
    public List<AccountSummaryResponse> getOtherAccounts(@CurrentLogin String login) {
        return accountService.getOtherAccounts(login).stream()
                .map(AccountSummaryResponse::from)
                .toList();
    }
}
