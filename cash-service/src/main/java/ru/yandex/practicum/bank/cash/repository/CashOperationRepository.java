package ru.yandex.practicum.bank.cash.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.yandex.practicum.bank.cash.model.CashOperation;

public interface CashOperationRepository extends ListCrudRepository<CashOperation, Long> {
}
