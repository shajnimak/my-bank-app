package ru.yandex.practicum.bank.front.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakLogoutSuccessHandlerTest {

    private final KeycloakLogoutSuccessHandler handler = new KeycloakLogoutSuccessHandler(
            "http://localhost:8180/realms/bank/protocol/openid-connect/logout");

    @Test
    void redirectsToAuthorizationServerLogoutWithIdTokenHint() throws Exception {
        var idToken = new OidcIdToken("id-token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "1", "preferred_username", "ivanov"));
        var user = new DefaultOidcUser(List.of(), idToken, "preferred_username");
        var authentication = new OAuth2AuthenticationToken(user, List.of(), "keycloak");
        var request = new MockHttpServletRequest("POST", "/logout");
        request.setServerPort(8080);
        var response = new MockHttpServletResponse();

        handler.onLogoutSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo(
                "http://localhost:8180/realms/bank/protocol/openid-connect/logout"
                        + "?post_logout_redirect_uri=http://localhost:8080/&id_token_hint=id-token");
    }
}
