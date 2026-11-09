package ru.yandex.practicum.bank.accounts.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    private final Clock clock;

    public AdultValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        return birthdate == null || !birthdate.plusYears(Adult.MIN_AGE).isAfter(LocalDate.now(clock));
    }
}
