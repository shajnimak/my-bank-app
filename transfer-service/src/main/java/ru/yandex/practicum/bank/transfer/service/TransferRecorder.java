package ru.yandex.practicum.bank.transfer.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferStatus;
import ru.yandex.practicum.bank.transfer.outbox.NotificationOutbox;
import ru.yandex.practicum.bank.transfer.repository.TransferRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Сохраняет результат перевода и уведомления участникам в одной транзакции.
 */
@Component
public class TransferRecorder {

    private final TransferRepository transferRepository;
    private final NotificationOutbox notificationOutbox;
    private final Clock clock;

    public TransferRecorder(TransferRepository transferRepository, NotificationOutbox notificationOutbox, Clock clock) {
        this.transferRepository = transferRepository;
        this.notificationOutbox = notificationOutbox;
        this.clock = clock;
    }

    @Transactional
    public Transfer recordCompleted(String fromLogin, String toLogin, long amount, long senderBalance) {
        Transfer transfer = save(fromLogin, toLogin, amount, TransferStatus.COMPLETED);
        notificationOutbox.enqueue(fromLogin, "Переведено %d руб клиенту %s, баланс %d руб"
                .formatted(amount, toLogin, senderBalance));
        notificationOutbox.enqueue(toLogin, "Получено %d руб от клиента %s".formatted(amount, fromLogin));
        return transfer;
    }

    @Transactional
    public Transfer recordFailed(String fromLogin, String toLogin, long amount, TransferStatus status) {
        return save(fromLogin, toLogin, amount, status);
    }

    private Transfer save(String fromLogin, String toLogin, long amount, TransferStatus status) {
        return transferRepository.save(Transfer.create(fromLogin, toLogin, amount, status, Instant.now(clock)));
    }
}
