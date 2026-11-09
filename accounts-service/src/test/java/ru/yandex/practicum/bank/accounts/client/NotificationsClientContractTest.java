package ru.yandex.practicum.bank.accounts.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.accounts.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificationsClientContractTest extends IntegrationTestBase {

    @Autowired
    private NotificationsClient notificationsClient;

    @Test
    void sendDeliversNotificationAccordingToContract() {
        assertThatCode(() -> notificationsClient.send("ivanov", "Данные аккаунта изменены"))
                .doesNotThrowAnyException();
    }
}
