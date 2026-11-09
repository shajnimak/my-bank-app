package ru.yandex.practicum.bank.cash.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.cash.model.CashAction;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.bank.cash.outbox.NotificationOutbox;
import ru.yandex.practicum.bank.cash.repository.CashOperationRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Сохраняет выполненную операцию и уведомление о ней в одной транзакции.
 */
@Component
public class CashOperationRecorder {

    private final CashOperationRepository cashOperationRepository;
    private final NotificationOutbox notificationOutbox;
    private final Clock clock;

    public CashOperationRecorder(CashOperationRepository cashOperationRepository,
                                 NotificationOutbox notificationOutbox,
                                 Clock clock) {
        this.cashOperationRepository = cashOperationRepository;
        this.notificationOutbox = notificationOutbox;
        this.clock = clock;
    }

    @Transactional
    public CashOperation record(String login, CashAction action, long amount, long balance) {
        CashOperation operation = cashOperationRepository.save(
                CashOperation.create(login, action, amount, balance, Instant.now(clock)));
        notificationOutbox.enqueue(login, notificationMessage(action, amount, balance));
        return operation;
    }

    private static String notificationMessage(CashAction action, long amount, long balance) {
        return switch (action) {
            case DEPOSIT -> "Счёт пополнен на %d руб, баланс %d руб".formatted(amount, balance);
            case WITHDRAW -> "Со счёта снято %d руб, баланс %d руб".formatted(amount, balance);
        };
    }
}
