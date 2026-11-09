package ru.yandex.practicum.bank.transfer.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.yandex.practicum.bank.transfer.model.Transfer;

public interface TransferRepository extends ListCrudRepository<Transfer, Long> {
}
