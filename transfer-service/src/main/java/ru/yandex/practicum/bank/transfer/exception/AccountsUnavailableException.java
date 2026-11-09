package ru.yandex.practicum.bank.transfer.exception;

public class AccountsUnavailableException extends RuntimeException {

    public AccountsUnavailableException(Throwable cause) {
        super("Сервис аккаунтов временно недоступен, попробуйте позже", cause);
    }
}
