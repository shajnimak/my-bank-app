package ru.yandex.practicum.bank.front.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * После выхода из фронта завершает сессию пользователя на сервере авторизации (OIDC RP-Initiated Logout).
 */
public class KeycloakLogoutSuccessHandler implements LogoutSuccessHandler {

    private final String logoutUri;

    public KeycloakLogoutSuccessHandler(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        String postLogoutRedirectUri = ServletUriComponentsBuilder.fromContextPath(request).path("/").toUriString();
        UriComponentsBuilder redirect = UriComponentsBuilder.fromUriString(logoutUri)
                .queryParam("post_logout_redirect_uri", postLogoutRedirectUri);
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser user) {
            redirect.queryParam("id_token_hint", user.getIdToken().getTokenValue());
        }
        response.sendRedirect(redirect.encode().toUriString());
    }
}
