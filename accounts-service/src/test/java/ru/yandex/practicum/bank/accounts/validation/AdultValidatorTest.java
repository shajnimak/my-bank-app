package ru.yandex.practicum.bank.accounts.validation;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AdultValidatorTest {

    private final AdultValidator validator = new AdultValidator(
            Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void acceptsPersonWhoTurnedEighteenToday() {
        assertThat(validator.isValid(LocalDate.of(2008, 11, 9), null)).isTrue();
    }

    @Test
    void rejectsPersonYoungerThanEighteen() {
        assertThat(validator.isValid(LocalDate.of(2008, 11, 10), null)).isFalse();
    }

    @Test
    void leavesMissingDateToNotNullConstraint() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
