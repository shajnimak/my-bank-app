package ru.yandex.practicum.bank.front.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.yandex.practicum.bank.front.client.BankApiException;
import ru.yandex.practicum.bank.front.controller.dto.CashAction;
import ru.yandex.practicum.bank.front.service.AccountService;
import ru.yandex.practicum.bank.front.service.CashService;
import ru.yandex.practicum.bank.front.service.MainPage;
import ru.yandex.practicum.bank.front.service.MainPageService;
import ru.yandex.practicum.bank.front.service.TransferService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Контроллер main.html.
 *
 * Модель main.html:
 *      login - логин текущего пользователя
 *      name - фамилия и имя текущего пользователя
 *      birthdate - дата рождения текущего пользователя в формате 'YYYY-MM-DD'
 *      sum - сумма на счету текущего пользователя
 *      accounts - аккаунты, которым можно перевести деньги
 *      errors - ошибки после выполнения действия (не обязательное)
 *      info - сообщение об успешном выполнении действия (не обязательное)
 */
@Controller
public class MainController {

    private static final String MAIN_VIEW = "main";

    private final MainPageService mainPageService;
    private final AccountService accountService;
    private final CashService cashService;
    private final TransferService transferService;

    public MainController(MainPageService mainPageService,
                          AccountService accountService,
                          CashService cashService,
                          TransferService transferService) {
        this.mainPageService = mainPageService;
        this.accountService = accountService;
        this.cashService = cashService;
        this.transferService = transferService;
    }

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model, Principal principal) {
        fillModel(model, principal, List.of(), null);
        return MAIN_VIEW;
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            Principal principal,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "birthdate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate birthdate
    ) {
        return perform(model, principal, () -> accountService.updateAccount(name, birthdate));
    }

    @PostMapping("/cash")
    public String editCash(
            Model model,
            Principal principal,
            @RequestParam(name = "value", required = false) Long value,
            @RequestParam("action") CashAction action
    ) {
        return perform(model, principal, () -> cashService.process(action, value));
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            Principal principal,
            @RequestParam(name = "value", required = false) Long value,
            @RequestParam(name = "login", required = false) String login
    ) {
        return perform(model, principal, () -> transferService.transfer(login, value));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String handleInvalidForm(Model model, Principal principal) {
        fillModel(model, principal, List.of("Форма заполнена некорректно"), null);
        return MAIN_VIEW;
    }

    private String perform(Model model, Principal principal, Supplier<String> action) {
        try {
            fillModel(model, principal, List.of(), action.get());
        } catch (BankApiException e) {
            fillModel(model, principal, e.getErrors(), null);
        }
        return MAIN_VIEW;
    }

    private void fillModel(Model model, Principal principal, List<String> actionErrors, String info) {
        List<String> errors = new ArrayList<>(actionErrors);
        try {
            MainPage page = mainPageService.load();
            model.addAttribute("name", page.name());
            model.addAttribute("birthdate", page.birthdate().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("sum", page.sum());
            model.addAttribute("accounts", page.accounts());
        } catch (BankApiException e) {
            errors.addAll(e.getErrors());
            model.addAttribute("accounts", List.of());
        }
        model.addAttribute("login", principal.getName());
        model.addAttribute("errors", errors.isEmpty() ? null : errors);
        model.addAttribute("info", info);
    }
}
