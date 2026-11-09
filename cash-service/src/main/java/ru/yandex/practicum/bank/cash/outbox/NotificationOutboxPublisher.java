package ru.yandex.practicum.bank.cash.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.cash.client.NotificationsClient;
import ru.yandex.practicum.bank.cash.model.OutboxNotification;
import ru.yandex.practicum.bank.cash.repository.OutboxNotificationRepository;

import java.time.Clock;
import java.time.Instant;

@Component
public class NotificationOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxPublisher.class);

    private final OutboxNotificationRepository outboxRepository;
    private final NotificationsClient notificationsClient;
    private final OutboxProperties properties;
    private final Clock clock;

    public NotificationOutboxPublisher(OutboxNotificationRepository outboxRepository,
                                       NotificationsClient notificationsClient,
                                       OutboxProperties properties,
                                       Clock clock) {
        this.outboxRepository = outboxRepository;
        this.notificationsClient = notificationsClient;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${bank.outbox.publish-delay:5s}")
    public void publishPending() {
        for (OutboxNotification notification : outboxRepository.findUnsent(properties.batchSize())) {
            try {
                notificationsClient.send(notification.login(), notification.message());
            } catch (RuntimeException e) {
                log.warn("Не удалось отправить уведомление {}: {}", notification.id(), e.getMessage());
                return;
            }
            outboxRepository.markSent(notification.id(), Instant.now(clock));
        }
    }
}
