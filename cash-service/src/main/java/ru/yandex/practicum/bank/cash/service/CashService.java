package ru.yandex.practicum.bank.cash.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.cash.client.AccountsClient;
import ru.yandex.practicum.bank.cash.model.CashAction;
import ru.yandex.practicum.bank.cash.model.CashOperation;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final CashOperationRecorder cashOperationRecorder;

    public CashService(AccountsClient accountsClient, CashOperationRecorder cashOperationRecorder) {
        this.accountsClient = accountsClient;
        this.cashOperationRecorder = cashOperationRecorder;
    }

    public CashOperation process(String login, CashAction action, long amount) {
        long balance = switch (action) {
            case DEPOSIT -> accountsClient.deposit(login, amount);
            case WITHDRAW -> accountsClient.withdraw(login, amount);
        };
        return cashOperationRecorder.record(login, action, amount, balance);
    }
}
