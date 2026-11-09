package ru.yandex.practicum.bank.transfer.exception;

public class SameAccountTransferException extends RuntimeException {

    public SameAccountTransferException() {
        super("Нельзя перевести деньги на свой счёт");
    }
}
