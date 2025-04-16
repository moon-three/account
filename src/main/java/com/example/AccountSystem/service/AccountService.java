package com.example.AccountSystem.service;


import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import com.example.AccountSystem.dto.AccountDTO;
import com.example.AccountSystem.exception.AccountException;
import com.example.AccountSystem.repository.AccountRepository;
import com.example.AccountSystem.repository.AccountUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.example.AccountSystem.type.AccountStatus.IN_USE;
import static com.example.AccountSystem.type.AccountStatus.UNREGISTERED;
import static com.example.AccountSystem.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDTO createAccount(Long userId, Long initialBalance) {
        AccountUser user = getAccountUser(userId);

        validateCreateAccount(user);

        String AccountNumber = createAccountNumber();

        return AccountDTO.fromEntity(accountRepository.save(
                    Account.builder()
                            .accountUser(user)
                            .accountNumber(AccountNumber)
                            .accountStatus(IN_USE)
                            .balance(initialBalance)
                            .registeredAt(LocalDateTime.now())
                            .build()
        ));
    }

    private String createAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf(
                    ThreadLocalRandom.current().nextLong(
                            1000000000L, 10000000000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private void validateCreateAccount(AccountUser accountUser) {
        // 계좌가 10개인 경우
        if(accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public AccountDTO deleteAccount(Long userId, String accountNumber) {
        AccountUser user = getAccountUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateDeleteAccount(user, account);

        account.unregister();

        accountRepository.save(account);

        return AccountDTO.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser user, Account account) {
        if(!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        if(account.getAccountStatus() == UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }

    public List<AccountDTO> getAccountByUserId(Long userId) {
        AccountUser user = getAccountUser(userId);

        List<Account> accounts = accountRepository.findByAccountUser(user);

        return accounts.stream().map(AccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
    }
}
