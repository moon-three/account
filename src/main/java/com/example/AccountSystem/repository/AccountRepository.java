package com.example.AccountSystem.repository;

import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Integer countByAccountUser(AccountUser accountUser);

    Boolean existsByAccountNumber(String accountNumber);
}
