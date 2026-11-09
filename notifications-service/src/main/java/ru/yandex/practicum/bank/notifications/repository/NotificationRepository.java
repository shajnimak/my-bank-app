package ru.yandex.practicum.bank.notifications.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.yandex.practicum.bank.notifications.model.Notification;

public interface NotificationRepository extends ListCrudRepository<Notification, Long> {
}
