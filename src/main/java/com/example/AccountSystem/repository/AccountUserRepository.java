package com.example.AccountSystem.repository;

import com.example.AccountSystem.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
    // findById 메서드는 기본으로 제공해준다
}
