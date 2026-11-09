package ru.yandex.practicum.bank.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.bank.accounts.validation.Adult;

import java.time.LocalDate;

public record UpdateAccountRequest(
        @NotBlank(message = "Фамилия и имя должны быть заполнены")
        @Size(max = 255, message = "Фамилия и имя должны быть не длиннее 255 символов")
        String name,
        @NotNull(message = "Дата рождения должна быть заполнена")
        @Adult
        LocalDate birthdate
) {
}
