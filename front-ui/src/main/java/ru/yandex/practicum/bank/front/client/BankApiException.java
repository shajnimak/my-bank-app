package ru.yandex.practicum.bank.front.client;

import java.util.List;

/**
 * Ошибка выполнения запроса в микросервисы банка с сообщениями для пользователя.
 */
public class BankApiException extends RuntimeException {

    private final List<String> errors;

    public BankApiException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
