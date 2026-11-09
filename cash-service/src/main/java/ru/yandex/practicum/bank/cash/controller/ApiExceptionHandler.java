package ru.yandex.practicum.bank.cash.controller;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.bank.cash.dto.ErrorResponse;
import ru.yandex.practicum.bank.cash.exception.AccountOperationRejectedException;
import ru.yandex.practicum.bank.cash.exception.AccountsUnavailableException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        return new ErrorResponse(e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .toList());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadable(HttpMessageNotReadableException e) {
        return ErrorResponse.of("Некорректный формат запроса");
    }

    @ExceptionHandler(AccountOperationRejectedException.class)
    public ResponseEntity<ErrorResponse> handleRejected(AccountOperationRejectedException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getErrors()));
    }

    @ExceptionHandler(AccountsUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleUnavailable(AccountsUnavailableException e) {
        return ErrorResponse.of(e.getMessage());
    }
}
