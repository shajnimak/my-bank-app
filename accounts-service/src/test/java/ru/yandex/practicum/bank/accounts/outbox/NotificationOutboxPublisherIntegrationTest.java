package ru.yandex.practicum.bank.accounts.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.accounts.IntegrationTestBase;
import ru.yandex.practicum.bank.accounts.model.OutboxNotification;
import ru.yandex.practicum.bank.accounts.repository.OutboxNotificationRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationOutboxPublisherIntegrationTest extends IntegrationTestBase {

    @Autowired
    private NotificationOutboxPublisher publisher;

    @Autowired
    private OutboxNotificationRepository outboxRepository;

    @Test
    void publishPendingSendsNotificationsAndMarksThemSent() {
        outboxRepository.save(OutboxNotification.create("ivanov", "Данные аккаунта изменены", Instant.now()));

        publisher.publishPending();

        assertThat(outboxRepository.findUnsent(10)).isEmpty();
        assertThat(outboxRepository.findAll()).singleElement()
                .satisfies(notification -> assertThat(notification.sentAt()).isNotNull());
    }
}
