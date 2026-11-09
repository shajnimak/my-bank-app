package ru.yandex.practicum.bank.front.client;

import org.springframework.web.client.HttpClientErrorException;
import ru.yandex.practicum.bank.front.client.dto.ErrorResponse;

import java.util.List;

final class ApiErrors {

    static final String SERVICE_UNAVAILABLE = "Сервис временно недоступен, попробуйте позже";
    static final String ACCESS_DENIED = "Недостаточно прав для выполнения операции";
    static final String REQUEST_REJECTED = "Операция отклонена";

    private ApiErrors() {
    }

    static BankApiException translate(Throwable throwable) {
        if (throwable instanceof HttpClientErrorException.Unauthorized
                || throwable instanceof HttpClientErrorException.Forbidden) {
            return new BankApiException(List.of(ACCESS_DENIED));
        }
        if (throwable instanceof HttpClientErrorException e) {
            return new BankApiException(errorsOf(e));
        }
        return new BankApiException(List.of(SERVICE_UNAVAILABLE));
    }

    private static List<String> errorsOf(HttpClientErrorException e) {
        try {
            ErrorResponse body = e.getResponseBodyAs(ErrorResponse.class);
            if (body != null && body.errors() != null && !body.errors().isEmpty()) {
                return body.errors();
            }
        } catch (RuntimeException ignored) {
            // тело ответа не в формате ErrorResponse
        }
        return List.of(REQUEST_REJECTED);
    }
}
