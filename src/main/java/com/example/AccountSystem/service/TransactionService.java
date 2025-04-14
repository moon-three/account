package com.example.AccountSystem.service;

import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import com.example.AccountSystem.domain.Transaction;
import com.example.AccountSystem.dto.TransactionDTO;
import com.example.AccountSystem.exception.AccountException;
import com.example.AccountSystem.repository.AccountRepository;
import com.example.AccountSystem.repository.AccountUserRepository;
import com.example.AccountSystem.repository.TransactionRepository;
import com.example.AccountSystem.type.AccountStatus;
import com.example.AccountSystem.type.TransactionResultType;
import com.example.AccountSystem.type.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.AccountSystem.type.ErrorCode.*;
import static com.example.AccountSystem.type.TransactionResultType.F;
import static com.example.AccountSystem.type.TransactionResultType.S;
import static com.example.AccountSystem.type.TransactionType.CANCEL;
import static com.example.AccountSystem.type.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDTO useBalance(
            Long userId, String accountNumber, Long amount) {
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDTO.fromEntity(
                saveAndGetTransaction(USE, S, account, amount));
    }

    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        // 사용자 아이디와 계좌 소유주가 다른 경우
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        // 계좌가 이미 해지된 경우
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 거래금액이 잔액보다 큰 경우
        if (account.getBalance() < amount) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }
    /** 사용 실패 */
    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(USE, F, account, amount);
    }

    @Transactional
    public TransactionDTO cancelBalance(
            String transactionId,
            String accountNumber,
            Long amount) {

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDTO.fromEntity(
                saveAndGetTransaction(CANCEL, S, account, amount)
        );
    }

    private void validateCancelBalance(
            Transaction transaction, Account account, Long amount) {
        // 거래금액과 취소금액이 다른 경우
        if(!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(CANCEL_MUST_FULLY);
        }
        // 해당 계좌의 거래가 아닌 경우
        if(!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(TRANSACTION_ACCOUNT_UN_MATCH);
        }
    }

    /** 취소 실패 */
    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(CANCEL, F, account, amount);
    }

    private Transaction saveAndGetTransaction(
            TransactionType transactionType,
            TransactionResultType transactionResultType,
            Account account,
            Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapShot(account.getBalance())
                        .transactionId(UUID.randomUUID()
                                .toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build());
    }

    public TransactionDTO queryTransaction(String transactionId) {
        return TransactionDTO.fromEntity(
                transactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND)));
    }
}
