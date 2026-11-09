package ru.yandex.practicum.bank.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.notifications.model.Notification;
import ru.yandex.practicum.bank.notifications.repository.NotificationRepository;

import java.time.Clock;
import java.time.Instant;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(NotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Transactional
    public Notification notify(String login, String message) {
        Notification notification = notificationRepository.save(
                Notification.create(login, message, Instant.now(clock)));
        log.info("Уведомление для {}: {}", login, message);
        return notification;
    }
}
