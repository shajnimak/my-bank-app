package ru.yandex.practicum.bank.accounts.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultValidator.class)
public @interface Adult {

    int MIN_AGE = 18;

    String message() default "Возраст должен быть старше 18 лет";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
