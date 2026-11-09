package ru.yandex.practicum.bank.accounts.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import ru.yandex.practicum.bank.accounts.model.Account;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends ListCrudRepository<Account, Long> {

    Optional<Account> findByLogin(String login);

    List<Account> findByLoginNotOrderByName(String login);

    @Modifying
    @Query("UPDATE accounts SET name = :name, birthdate = :birthdate WHERE login = :login")
    boolean updateProfile(String login, String name, LocalDate birthdate);

    @Modifying
    @Query("UPDATE accounts SET balance = balance + :amount WHERE login = :login")
    boolean deposit(String login, long amount);

    @Modifying
    @Query("UPDATE accounts SET balance = balance - :amount WHERE login = :login AND balance >= :amount")
    boolean withdraw(String login, long amount);
}
