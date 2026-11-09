package ru.yandex.practicum.bank.front.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.front.client.AccountsClient;
import ru.yandex.practicum.bank.front.client.TransferClient;
import ru.yandex.practicum.bank.front.client.dto.AccountSummaryResponse;
import ru.yandex.practicum.bank.front.client.dto.TransferRequest;

@Service
public class TransferService {

    private final TransferClient transferClient;
    private final AccountsClient accountsClient;

    public TransferService(TransferClient transferClient, AccountsClient accountsClient) {
        this.transferClient = transferClient;
        this.accountsClient = accountsClient;
    }

    public String transfer(String login, Long value) {
        transferClient.transfer(new TransferRequest(login, value));
        return "Успешно переведено %d руб клиенту %s".formatted(value, recipientName(login));
    }

    private String recipientName(String login) {
        return accountsClient.getOtherAccounts().stream()
                .filter(account -> account.login().equals(login))
                .map(AccountSummaryResponse::name)
                .findFirst()
                .orElse(login);
    }
}
