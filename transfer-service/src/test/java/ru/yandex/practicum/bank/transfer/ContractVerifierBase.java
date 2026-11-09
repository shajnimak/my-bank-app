package ru.yandex.practicum.bank.transfer;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.transfer.config.SecurityConfig;
import ru.yandex.practicum.bank.transfer.controller.TransferController;
import ru.yandex.practicum.bank.transfer.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferStatus;
import ru.yandex.practicum.bank.transfer.service.TransferService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
public abstract class ContractVerifierBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpContract() {
        when(transferService.transfer(eq("ivanov"), anyString(), anyLong())).thenAnswer(invocation -> new Transfer(
                1L, "ivanov", invocation.getArgument(1), invocation.getArgument(2), TransferStatus.COMPLETED,
                Instant.parse("2026-11-09T10:15:30Z")));
        when(transferService.transfer(eq("ivanov"), anyString(), eq(1_000_000L))).thenThrow(
                new AccountOperationRejectedException(HttpStatus.CONFLICT, List.of("Недостаточно средств на счету")));

        RestAssuredMockMvc.mockMvc(mockMvc);
        RestAssuredMockMvc.postProcessors(jwt()
                .jwt(token -> token.claim("preferred_username", "ivanov"))
                .authorities(new SimpleGrantedAuthority("SCOPE_transfer")));
    }
}
