package ru.yandex.practicum.bank.accounts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.accounts.config.ClockConfig;
import ru.yandex.practicum.bank.accounts.config.SecurityConfig;
import ru.yandex.practicum.bank.accounts.controller.AccountController;
import ru.yandex.practicum.bank.accounts.controller.BalanceController;
import ru.yandex.practicum.bank.accounts.exception.AccountNotFoundException;
import ru.yandex.practicum.bank.accounts.exception.InsufficientFundsException;
import ru.yandex.practicum.bank.accounts.model.Account;
import ru.yandex.practicum.bank.accounts.service.AccountService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest({AccountController.class, BalanceController.class})
@Import({SecurityConfig.class, ClockConfig.class})
public abstract class ContractVerifierBase {

    private static final LocalDate IVANOV_BIRTHDATE = LocalDate.of(2001, 1, 1);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpContract() {
        when(accountService.getAccount("ivanov"))
                .thenReturn(new Account(1L, "ivanov", "Иванов Иван", IVANOV_BIRTHDATE, 100));
        when(accountService.updateProfile(eq("ivanov"), anyString(), any())).thenAnswer(invocation ->
                new Account(1L, "ivanov", invocation.getArgument(1), invocation.getArgument(2), 100));
        when(accountService.getOtherAccounts("ivanov")).thenReturn(List.of(
                new Account(2L, "petrov", "Петров Петр", LocalDate.of(1995, 5, 15), 100),
                new Account(3L, "sidorov", "Сидоров Сидор", LocalDate.of(1988, 9, 30), 100)));
        when(accountService.deposit(anyString(), anyLong())).thenAnswer(invocation ->
                new Account(2L, invocation.getArgument(0), "Петров Петр", LocalDate.of(1995, 5, 15), 150));
        when(accountService.deposit(eq("unknown"), anyLong())).thenThrow(new AccountNotFoundException("unknown"));
        when(accountService.withdraw(anyString(), anyLong())).thenAnswer(invocation ->
                new Account(1L, invocation.getArgument(0), "Иванов Иван", IVANOV_BIRTHDATE, 50));
        when(accountService.withdraw(anyString(), eq(1_000_000L))).thenThrow(new InsufficientFundsException());

        RestAssuredMockMvc.mockMvc(mockMvc);
        RestAssuredMockMvc.postProcessors(jwt()
                .jwt(token -> token.claim("preferred_username", "ivanov"))
                .authorities(new SimpleGrantedAuthority("SCOPE_accounts"),
                        new SimpleGrantedAuthority("SCOPE_accounts.balance")));
    }
}
