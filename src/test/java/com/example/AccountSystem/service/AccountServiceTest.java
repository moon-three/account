package com.example.AccountSystem.service;

import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import com.example.AccountSystem.dto.AccountDTO;
import com.example.AccountSystem.exception.AccountException;
import com.example.AccountSystem.repository.AccountRepository;
import com.example.AccountSystem.repository.AccountUserRepository;
import com.example.AccountSystem.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.AccountSystem.type.AccountStatus.UNREGISTERED;
import static com.example.AccountSystem.type.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1234567890")
                        .build());

        // when
        AccountDTO accountDTO = accountService.createAccount(
                1L, 10000L);

        // then
        assertEquals(1L, accountDTO.getUserId());
        assertEquals("1234567890", accountDTO.getAccountNumber());
    }

    @Test
    @DisplayName("해당 사용자 없음 - 계좌 생성 실패")
    void createAccountFailed_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌번호 중복 테스트")
    void createAccount_DuplicatedAccountNumber() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.existsByAccountNumber(anyString()))
                .willReturn(true)
                .willReturn(false);

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1234567890")
                        .build());

        // when
        accountService.createAccount(1L, 10000L);

        // then
        verify(accountRepository, times(2))
                .existsByAccountNumber(anyString());
    }

    @Test
    @DisplayName("사용자 당 최대 계좌 수는 10개 - 계좌 생성 실패")
    void createAccountFailed_maxAccountIs10() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 10000L));

        // then
        assertEquals(MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    void deleteAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1234567890")
                        .unRegisteredAt(LocalDateTime.now())
                        .build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDTO accountDTO = accountService.deleteAccount(1L, "1234567890");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(1L, accountDTO.getUserId());
        assertEquals("1234567890", accountDTO.getAccountNumber());
        assertEquals(UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 사용자 없음 - 계좌 해지 실패")
    void deleteAccountFailed_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccountFailed_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자와 계좌 소유주가 다름 - 계좌 해지 실패")
    void deleteAccountFailed_UserUnMatch() {
        // given
        AccountUser MinSu = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        AccountUser Mina = AccountUser.builder()
                .id(2L)
                .name("Mina")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(MinSu));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Mina)
                        .balance(0L)
                        .accountNumber("1234567890")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지된 계좌 - 계좌 해지 실패")
    void deleteAccountFailed_AlreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1234567890")
                        .accountStatus(UNREGISTERED)
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 잔액이 0이 아님 - 계좌 해지 실패")
    void deleteAccountFailed_balanceNotEmpty() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(1000L)
                        .accountNumber("1234567890")
                        .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(BALANCE_NOT_EMPTY, exception.getErrorCode());
    }


    @Test
    void successGetAccountByUserId() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("MinSu")
                .build();

        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        // when
        List<AccountDTO> accountDTOS = accountService.getAccountByUserId(1L);

        // then
        assertEquals(3, accountDTOS.size());
        assertEquals("1111111111", accountDTOS.get(0).getAccountNumber());
        assertEquals(1000L, accountDTOS.get(0).getBalance());
        assertEquals("2222222222", accountDTOS.get(1).getAccountNumber());
        assertEquals(2000L, accountDTOS.get(1).getBalance());
        assertEquals("3333333333", accountDTOS.get(2).getAccountNumber());
        assertEquals(3000L, accountDTOS.get(2).getBalance());
    }

    @Test
    @DisplayName("해당 사용자 없음 - 계좌 조회 실패")
    void failedToGetAccounts() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }


}