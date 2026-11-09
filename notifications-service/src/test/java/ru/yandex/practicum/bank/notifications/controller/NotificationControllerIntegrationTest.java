package ru.yandex.practicum.bank.notifications.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.notifications.TestcontainersConfiguration;
import ru.yandex.practicum.bank.notifications.model.Notification;
import ru.yandex.practicum.bank.notifications.repository.NotificationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class NotificationControllerIntegrationTest {

    private static final String BODY = """
            {"login": "ivanov", "message": "Положено 100 руб"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void cleanUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void notifySavesNotification() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_notifications")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.login").value("ivanov"))
                .andExpect(jsonPath("$.message").value("Положено 100 руб"));

        assertThat(notificationRepository.findAll())
                .extracting(Notification::login, Notification::message)
                .containsExactly(tuple("ivanov", "Положено 100 руб"));
    }

    @Test
    void notifyRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_notifications")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "", "message": "Положено 100 руб"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Не указан логин получателя уведомления"));
    }

    @Test
    void notifyRequiresNotificationsScope() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_accounts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void notifyRequiresToken() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }
}
