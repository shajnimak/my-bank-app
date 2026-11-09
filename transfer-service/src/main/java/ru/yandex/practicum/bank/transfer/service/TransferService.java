package ru.yandex.practicum.bank.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.transfer.client.AccountsClient;
import ru.yandex.practicum.bank.transfer.exception.SameAccountTransferException;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferStatus;

/**
 * Перевод выполняется как сага: списание у отправителя, зачисление получателю,
 * при ошибке зачисления деньги возвращаются отправителю.
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountsClient accountsClient;
    private final TransferRecorder transferRecorder;

    public TransferService(AccountsClient accountsClient, TransferRecorder transferRecorder) {
        this.accountsClient = accountsClient;
        this.transferRecorder = transferRecorder;
    }

    public Transfer transfer(String fromLogin, String toLogin, long amount) {
        if (fromLogin.equals(toLogin)) {
            throw new SameAccountTransferException();
        }
        long senderBalance = accountsClient.withdraw(fromLogin, amount);
        try {
            accountsClient.deposit(toLogin, amount);
        } catch (RuntimeException e) {
            compensate(fromLogin, toLogin, amount);
            throw e;
        }
        return transferRecorder.recordCompleted(fromLogin, toLogin, amount, senderBalance);
    }

    private void compensate(String fromLogin, String toLogin, long amount) {
        try {
            accountsClient.deposit(fromLogin, amount);
            transferRecorder.recordFailed(fromLogin, toLogin, amount, TransferStatus.FAILED);
        } catch (RuntimeException e) {
            log.error("Не удалось вернуть {} руб на счёт {} после неудачного перевода клиенту {}",
                    amount, fromLogin, toLogin, e);
            transferRecorder.recordFailed(fromLogin, toLogin, amount, TransferStatus.COMPENSATION_FAILED);
        }
    }
}
