package ru.yandex.practicum.bank.transfer.outbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.transfer.model.OutboxNotification;
import ru.yandex.practicum.bank.transfer.repository.OutboxNotificationRepository;

import java.time.Clock;
import java.time.Instant;

@Component
public class NotificationOutbox {

    private final OutboxNotificationRepository outboxRepository;
    private final Clock clock;

    public NotificationOutbox(OutboxNotificationRepository outboxRepository, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String login, String message) {
        outboxRepository.save(OutboxNotification.create(login, message, Instant.now(clock)));
    }
}
