package ru.yandex.practicum.bank.cash.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.cash.client.NotificationsClient;
import ru.yandex.practicum.bank.cash.model.OutboxNotification;
import ru.yandex.practicum.bank.cash.repository.OutboxNotificationRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxPublisherTest {

    private static final Instant NOW = Instant.parse("2026-11-09T10:15:30Z");

    @Mock
    private OutboxNotificationRepository outboxRepository;

    @Mock
    private NotificationsClient notificationsClient;

    private NotificationOutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new NotificationOutboxPublisher(outboxRepository, notificationsClient,
                new OutboxProperties(10), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void publishPendingMarksDeliveredNotificationsSent() {
        when(outboxRepository.findUnsent(10)).thenReturn(List.of(
                new OutboxNotification(1L, "ivanov", "first", NOW, null),
                new OutboxNotification(2L, "petrov", "second", NOW, null)));

        publisher.publishPending();

        verify(notificationsClient).send("ivanov", "first");
        verify(notificationsClient).send("petrov", "second");
        verify(outboxRepository).markSent(1L, NOW);
        verify(outboxRepository).markSent(2L, NOW);
    }

    @Test
    void publishPendingStopsOnDeliveryFailureAndKeepsNotificationUnsent() {
        when(outboxRepository.findUnsent(10)).thenReturn(List.of(
                new OutboxNotification(1L, "ivanov", "first", NOW, null),
                new OutboxNotification(2L, "petrov", "second", NOW, null)));
        doThrow(new IllegalStateException("unavailable")).when(notificationsClient).send("ivanov", "first");

        publisher.publishPending();

        verify(notificationsClient, never()).send("petrov", "second");
        verify(outboxRepository, never()).markSent(anyLong(), any());
    }
}
