package ru.yandex.practicum.bank.notifications;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.notifications.config.SecurityConfig;
import ru.yandex.practicum.bank.notifications.controller.NotificationController;
import ru.yandex.practicum.bank.notifications.model.Notification;
import ru.yandex.practicum.bank.notifications.service.NotificationService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
public abstract class ContractVerifierBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpContract() {
        when(notificationService.notify(anyString(), anyString())).thenAnswer(invocation -> new Notification(
                1L, invocation.getArgument(0), invocation.getArgument(1), Instant.parse("2026-11-09T10:15:30Z")));
        RestAssuredMockMvc.mockMvc(mockMvc);
        RestAssuredMockMvc.postProcessors(jwt().authorities(new SimpleGrantedAuthority("SCOPE_notifications")));
    }
}
