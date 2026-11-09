package ru.yandex.practicum.bank.front;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

public final class TestUsers {

    private TestUsers() {
    }

    public static RequestPostProcessor user() {
        return oauth2Login().oauth2User(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("preferred_username", "ivanov"),
                "preferred_username"));
    }
}
