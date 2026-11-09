package ru.yandex.practicum.bank.cash;

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
import ru.yandex.practicum.bank.cash.config.SecurityConfig;
import ru.yandex.practicum.bank.cash.controller.CashController;
import ru.yandex.practicum.bank.cash.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.cash.model.CashAction;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.bank.cash.service.CashService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(CashController.class)
@Import(SecurityConfig.class)
public abstract class ContractVerifierBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashService cashService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpContract() {
        when(cashService.process(eq("ivanov"), eq(CashAction.DEPOSIT), anyLong())).thenAnswer(invocation ->
                operation(CashAction.DEPOSIT, invocation.getArgument(2), 200));
        when(cashService.process(eq("ivanov"), eq(CashAction.WITHDRAW), anyLong())).thenAnswer(invocation ->
                operation(CashAction.WITHDRAW, invocation.getArgument(2), 0));
        when(cashService.process("ivanov", CashAction.WITHDRAW, 1_000_000L)).thenThrow(
                new AccountOperationRejectedException(HttpStatus.CONFLICT, List.of("Недостаточно средств на счету")));

        RestAssuredMockMvc.mockMvc(mockMvc);
        RestAssuredMockMvc.postProcessors(jwt()
                .jwt(token -> token.claim("preferred_username", "ivanov"))
                .authorities(new SimpleGrantedAuthority("SCOPE_cash")));
    }

    private static CashOperation operation(CashAction action, long amount, long balance) {
        return new CashOperation(1L, "ivanov", action, amount, balance, Instant.parse("2026-11-09T10:15:30Z"));
    }
}
