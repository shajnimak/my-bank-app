package ru.yandex.practicum.bank.front.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.front.client.BankApiException;
import ru.yandex.practicum.bank.front.config.SecurityConfig;
import ru.yandex.practicum.bank.front.controller.dto.AccountDto;
import ru.yandex.practicum.bank.front.controller.dto.CashAction;
import ru.yandex.practicum.bank.front.service.AccountService;
import ru.yandex.practicum.bank.front.service.CashService;
import ru.yandex.practicum.bank.front.service.MainPage;
import ru.yandex.practicum.bank.front.service.MainPageService;
import ru.yandex.practicum.bank.front.service.TransferService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.yandex.practicum.bank.front.TestUsers.user;

@WebMvcTest(MainController.class)
@Import(SecurityConfig.class)
class MainControllerTest {

    private static final MainPage PAGE = new MainPage("Иванов Иван", LocalDate.of(2001, 1, 1), 100,
            List.of(new AccountDto("petrov", "Петров Петр")));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MainPageService mainPageService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CashService cashService;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUpPage() {
        when(mainPageService.load()).thenReturn(PAGE);
    }

    @Test
    void indexRedirectsToAccountPage() throws Exception {
        mockMvc.perform(get("/").with(user()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));
    }

    @Test
    void accountPageFillsModel() throws Exception {
        mockMvc.perform(get("/account").with(user()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("login", "ivanov"))
                .andExpect(model().attribute("name", "Иванов Иван"))
                .andExpect(model().attribute("birthdate", "2001-01-01"))
                .andExpect(model().attribute("sum", 100L))
                .andExpect(model().attribute("accounts", contains(new AccountDto("petrov", "Петров Петр"))))
                .andExpect(model().attribute("errors", (Object) null))
                .andExpect(model().attribute("info", (Object) null));
    }

    @Test
    void unavailableAccountsServiceIsShownAsError() throws Exception {
        when(mainPageService.load()).thenThrow(new BankApiException(List.of("Сервис временно недоступен")));

        mockMvc.perform(get("/account").with(user()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Сервис временно недоступен")))
                .andExpect(model().attribute("accounts", empty()));
    }

    @Test
    void editAccountShowsInfo() throws Exception {
        when(accountService.updateAccount("Иванов Иван", LocalDate.of(2000, 5, 6)))
                .thenReturn("Данные аккаунта сохранены");

        mockMvc.perform(post("/account").with(user()).with(csrf())
                        .param("name", "Иванов Иван")
                        .param("birthdate", "2000-05-06"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Данные аккаунта сохранены"));
    }

    @Test
    void cashErrorIsShownWithPageData() throws Exception {
        when(cashService.process(CashAction.GET, 500L))
                .thenThrow(new BankApiException(List.of("Недостаточно средств на счету")));

        mockMvc.perform(post("/cash").with(user()).with(csrf())
                        .param("value", "500")
                        .param("action", "GET"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Недостаточно средств на счету")))
                .andExpect(model().attribute("sum", 100L));
    }

    @Test
    void transferShowsInfo() throws Exception {
        when(transferService.transfer("petrov", 10L)).thenReturn("Успешно переведено 10 руб клиенту Петров Петр");

        mockMvc.perform(post("/transfer").with(user()).with(csrf())
                        .param("value", "10")
                        .param("login", "petrov"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Успешно переведено 10 руб клиенту Петров Петр"));
        verify(transferService).transfer("petrov", 10L);
    }

    @Test
    void nonNumericAmountIsShownAsFormError() throws Exception {
        mockMvc.perform(post("/transfer").with(user()).with(csrf())
                        .param("value", "много")
                        .param("login", "petrov"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Форма заполнена некорректно")));
        verifyNoInteractions(transferService);
    }

    @Test
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/cash").with(user())
                        .param("value", "10")
                        .param("action", "PUT"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cashService);
    }
}
