package ru.yandex.practicum.bank.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.notifications.model.Notification;
import ru.yandex.practicum.bank.notifications.repository.NotificationRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-09T10:15:30Z");

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void notifySavesNotificationWithCurrentTime() {
        when(notificationRepository.save(any())).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            return new Notification(7L, notification.login(), notification.message(), notification.createdAt());
        });

        Notification saved = notificationService.notify("ivanov", "Положено 100 руб");

        assertThat(saved).isEqualTo(new Notification(7L, "ivanov", "Положено 100 руб", NOW));
        verify(notificationRepository).save(Notification.create("ivanov", "Положено 100 руб", NOW));
    }
}
