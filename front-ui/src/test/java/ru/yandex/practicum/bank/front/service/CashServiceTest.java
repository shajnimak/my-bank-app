package ru.yandex.practicum.bank.front.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.front.client.CashClient;
import ru.yandex.practicum.bank.front.client.dto.CashOperationRequest;
import ru.yandex.practicum.bank.front.client.dto.CashOperationRequest.CashOperationType;
import ru.yandex.practicum.bank.front.controller.dto.CashAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private CashClient cashClient;

    @InjectMocks
    private CashService cashService;

    @Test
    void putDepositsMoney() {
        assertThat(cashService.process(CashAction.PUT, 100L)).isEqualTo("Положено 100 руб");
        verify(cashClient).process(new CashOperationRequest(CashOperationType.DEPOSIT, 100L));
    }

    @Test
    void getWithdrawsMoney() {
        assertThat(cashService.process(CashAction.GET, 30L)).isEqualTo("Снято 30 руб");
        verify(cashClient).process(new CashOperationRequest(CashOperationType.WITHDRAW, 30L));
    }
}
