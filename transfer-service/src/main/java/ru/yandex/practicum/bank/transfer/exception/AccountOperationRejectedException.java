package ru.yandex.practicum.bank.transfer.exception;

import org.springframework.http.HttpStatusCode;

import java.util.List;

/**
 * Сервис аккаунтов отклонил операцию со счётом (нет аккаунта, недостаточно средств и т.п.).
 */
public class AccountOperationRejectedException extends RuntimeException {

    private final HttpStatusCode status;
    private final List<String> errors;

    public AccountOperationRejectedException(HttpStatusCode status, List<String> errors) {
        super(String.join("; ", errors));
        this.status = status;
        this.errors = List.copyOf(errors);
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}
