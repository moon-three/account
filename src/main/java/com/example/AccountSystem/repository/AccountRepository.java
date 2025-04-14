package com.example.AccountSystem.repository;

import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Integer countByAccountUser(AccountUser accountUser);

    Boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(AccountUser user);
}
