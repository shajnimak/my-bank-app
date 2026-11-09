package ru.yandex.practicum.bank.cash.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import ru.yandex.practicum.bank.cash.model.OutboxNotification;

import java.time.Instant;
import java.util.List;

public interface OutboxNotificationRepository extends ListCrudRepository<OutboxNotification, Long> {

    @Query("SELECT * FROM outbox_notifications WHERE sent_at IS NULL ORDER BY id LIMIT :limit")
    List<OutboxNotification> findUnsent(int limit);

    @Modifying
    @Query("UPDATE outbox_notifications SET sent_at = :sentAt WHERE id = :id")
    void markSent(long id, Instant sentAt);
}
