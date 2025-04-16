package com.example.AccountSystem.service;

import com.example.AccountSystem.exception.AccountException;
import com.example.AccountSystem.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.AccountSystem.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final RedissonClient redissonClient;    // @Bean 이름과 같으면 자동 주입

    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber : {}", accountNumber);

        try {
            boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS); // 1초 동안 락을 시도하고, 성공하면 락을 5초 동안 유지
            if(!isLock) {
                log.error("==========Lock acquisition failed==========");
                throw new AccountException(ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed", e);
        }
    }

    public void unlock(String accountNumber) {
        log.debug("Unlock for accountNumber : {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }

    private static String getLockKey(String accountNumber) {
        return "ACLK: " + accountNumber;
    }
}
