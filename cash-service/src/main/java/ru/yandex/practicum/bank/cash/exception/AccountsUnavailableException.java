package ru.yandex.practicum.bank.cash.exception;

public class AccountsUnavailableException extends RuntimeException {

    public AccountsUnavailableException(Throwable cause) {
        super("Сервис аккаунтов временно недоступен, попробуйте позже", cause);
    }
}
