package ru.yandex.practicum.bank.transfer.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("bank.outbox")
public record OutboxProperties(@DefaultValue("50") int batchSize) {
}
