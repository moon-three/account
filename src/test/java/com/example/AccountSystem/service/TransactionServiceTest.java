package com.example.AccountSystem.service;

import com.example.AccountSystem.domain.Account;
import com.example.AccountSystem.domain.AccountUser;
import com.example.AccountSystem.domain.Transaction;
import com.example.AccountSystem.dto.TransactionDTO;
import com.example.AccountSystem.exception.AccountException;
import com.example.AccountSystem.repository.AccountRepository;
import com.example.AccountSystem.repository.AccountUserRepository;
import com.example.AccountSystem.repository.TransactionRepository;
import com.example.AccountSystem.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.AccountSystem.type.AccountStatus.IN_USE;
import static com.example.AccountSystem.type.AccountStatus.UNREGISTERED;
import static com.example.AccountSystem.type.ErrorCode.*;
import static com.example.AccountSystem.type.TransactionResultType.F;
import static com.example.AccountSystem.type.TransactionResultType.S;
import static com.example.AccountSystem.type.TransactionType.CANCEL;
import static com.example.AccountSystem.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(10000L)
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(S)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapShot(9000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDTO transactionDTO = transactionService.useBalance(
                1L, "1234567890", 5000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(5000L, captor.getValue().getAmount());
        assertEquals(5000L, captor.getValue().getBalanceSnapShot());
        assertEquals(USE, transactionDTO.getTransactionType());
        assertEquals(S, transactionDTO.getTransactionResultType());
        assertEquals(1000L, transactionDTO.getAmount());
        assertEquals(9000L, transactionDTO.getBalanceSnapShot());
    }

    @Test
    @DisplayName("해당 사용자 없음 - 잔액 사용 실패")
    void useBalanceFailed_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "1234567890", 1000L));

        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
    void useBalanceFailed_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "1234567890", 1000L));
        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    void useBalanceFailed_userUnMatch() {
        // given
        AccountUser MinSu = AccountUser.builder()
                .name("MinSu")
                .build();
        MinSu.setId(1L);

        AccountUser Mina = AccountUser.builder()
                .name("Mina")
                .build();
        Mina.setId(2L);

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
                () -> transactionService.useBalance(
                        1L, "1234567890", 1000L));

        // then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지된 계좌 - 잔액 사용 실패")
    void useBalanceFailed_AlreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

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
                () -> transactionService.useBalance(
                        1L, "1234567890", 1000L));

        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액보다 거래 금액이 더 큼 - 잔액 사용 실패")
    void useBalanceFailed_exceedAmount() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(100L)
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "1234567890", 1000L));

        // then
        verify(transactionRepository, times(0)).save(any());
        assertEquals(AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용실패 트랜잭션 저장테스트")
    void saveFailedUseTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(10000L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(S)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapShot(9000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        transactionService.saveFailedUseTransaction(
                "1234567890", 5000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(5000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapShot());
        assertEquals(F, captor.getValue().getTransactionResultType());
    }

    @Test
    void successCancelBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(50000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionType(CANCEL)
                .transactionResultType(S)
                .account(account)
                .amount(1000L)
                .balanceSnapShot(9000L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapShot(10000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDTO transactionDTO = transactionService.cancelBalance(
                "transactionId", "1234567890", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(51000L, captor.getValue().getBalanceSnapShot());
        assertEquals(CANCEL, transactionDTO.getTransactionType());
        assertEquals(S, transactionDTO.getTransactionResultType());
        assertEquals(1000L, transactionDTO.getAmount());
        assertEquals(10000L, transactionDTO.getBalanceSnapShot());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 거래 취소 실패")
    void cancelBalanceFailed_AccountNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId",
                        "1234567890", 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("해당 거래 없음 - 거래 취소 실패")
    void cancelBalanceFailed_TransactionNotFound() {
        // given

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId",
                        "1234567890",
                        1000L));
        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래금액과 취소금액이 다름 - 거래 취소 실패")
    void cancelBalanceFailed_CancelMustFully() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(50000L)
                .build();

        Transaction transaction = Transaction.builder()
                .transactionType(CANCEL)
                .transactionResultType(S)
                .account(account)
                .amount(1000L)
                .balanceSnapShot(9000L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId",
                "1234567890", 5000L));

        // then
        assertEquals(CANCEL_MUST_FULLY, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("해당 계좌의 거래가 아님 - 거래 취소 실패")
    void cancelBalanceFailed_TransactionAccountUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(50000L)
                .build();
        account.setId(1L);

        Account accountNotUse = Account.builder()
                .accountUser(user)
                .accountNumber("1111111111")
                .accountStatus(IN_USE)
                .balance(10000L)
                .build();
        accountNotUse.setId(2L);

        Transaction transaction = Transaction.builder()
                .transactionType(CANCEL)
                .transactionResultType(S)
                .account(account)
                .amount(1000L)
                .balanceSnapShot(9000L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUse));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId",
                        "1111111111", 1000L));

        // then
        assertEquals(TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("취소실패 트랜잭션 저장테스트")
    void saveFailedCancelTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(50000L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapShot(10000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        transactionService.saveFailedCancelTransaction(
                "1234567890", 5000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(5000L, captor.getValue().getAmount());
        assertEquals(50000L, captor.getValue().getBalanceSnapShot());
        assertEquals(F, captor.getValue().getTransactionResultType());
    }

    @Test
    void successQueryTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .name("MinSu")
                .build();
        user.setId(1L);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1234567890")
                .accountStatus(IN_USE)
                .balance(50000L)
                .build();
        account.setId(1L);

        Transaction transaction = Transaction.builder()
                .transactionType(USE)
                .transactionResultType(S)
                .account(account)
                .amount(1000L)
                .balanceSnapShot(9000L)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        // when
        TransactionDTO transactionDTO = transactionService.queryTransaction(
                "transactionId");

        // then
        assertEquals(USE, transactionDTO.getTransactionType());
        assertEquals(S, transactionDTO.getTransactionResultType());
        assertEquals(1000L, transactionDTO.getAmount());
        assertEquals("transactionId", transactionDTO.getTransactionId());
    }

    @Test
    @DisplayName("해당 거래 없음 - 거래 조회 실패")
    void QueryTransactionFailed_TransactionNotFound() {
        // given

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));

        // then
        assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

}