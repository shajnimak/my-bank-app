package ru.yandex.practicum.bank.front.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.front.client.CashClient;
import ru.yandex.practicum.bank.front.client.dto.CashOperationRequest;
import ru.yandex.practicum.bank.front.client.dto.CashOperationRequest.CashOperationType;
import ru.yandex.practicum.bank.front.controller.dto.CashAction;

@Service
public class CashService {

    private final CashClient cashClient;

    public CashService(CashClient cashClient) {
        this.cashClient = cashClient;
    }

    public String process(CashAction action, Long value) {
        return switch (action) {
            case PUT -> {
                cashClient.process(new CashOperationRequest(CashOperationType.DEPOSIT, value));
                yield "Положено %d руб".formatted(value);
            }
            case GET -> {
                cashClient.process(new CashOperationRequest(CashOperationType.WITHDRAW, value));
                yield "Снято %d руб".formatted(value);
            }
        };
    }
}
