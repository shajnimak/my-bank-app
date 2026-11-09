package ru.yandex.practicum.bank.cash.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.cash.IntegrationTestBase;
import ru.yandex.practicum.bank.cash.model.OutboxNotification;
import ru.yandex.practicum.bank.cash.repository.OutboxNotificationRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationOutboxPublisherIntegrationTest extends IntegrationTestBase {

    @Autowired
    private NotificationOutboxPublisher publisher;

    @Autowired
    private OutboxNotificationRepository outboxRepository;

    @Test
    void publishPendingSendsNotificationsAndMarksThemSent() {
        outboxRepository.save(OutboxNotification.create("ivanov", "Уведомление", Instant.now()));

        publisher.publishPending();

        assertThat(outboxRepository.findUnsent(10)).isEmpty();
        assertThat(outboxRepository.findAll()).singleElement()
                .satisfies(notification -> assertThat(notification.sentAt()).isNotNull());
    }
}
