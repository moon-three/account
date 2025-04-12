package com.example.AccountSystem.service;


import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import com.example.AccountSystem.dto.AccountDTO;
import com.example.AccountSystem.exception.AccountException;
import com.example.AccountSystem.repository.AccountRepository;
import com.example.AccountSystem.repository.AccountUserRepository;
import com.example.AccountSystem.type.AccountStatus;
import com.example.AccountSystem.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.AccountSystem.type.AccountStatus.IN_USE;
import static com.example.AccountSystem.type.AccountStatus.USE;
import static com.example.AccountSystem.type.ErrorCode.MAX_ACCOUNT_PER_USER_10;
import static com.example.AccountSystem.type.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDTO createAccount(Long userId, Long initialBalance) {
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        validateCreateAccount(user);

        String AccountNumber = getAccountNumber();

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

    private String getAccountNumber() {
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

}
