package ru.yandex.practicum.bank.transfer;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestJwt {

    private TestJwt() {
    }

    public static JwtRequestPostProcessor user(String login) {
        return jwt()
                .jwt(token -> token.claim("preferred_username", login))
                .authorities(new SimpleGrantedAuthority("SCOPE_transfer"));
    }
}
