package com.example.AccountSystem.dto;

import com.example.AccountSystem.aop.AccountLockIdInterface;
import com.example.AccountSystem.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class UseBalance {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request implements AccountLockIdInterface {
        @NotNull
        @Min(1)
        private Long userId;

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;

        @NotNull
        @Min(100)
        @Max(1_000_000_000)
        private Long amount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResult;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        public static Response from(TransactionDTO transactionDTO) {
            return Response.builder()
                    .accountNumber(transactionDTO.getAccountNumber())
                    .transactionResult(transactionDTO.getTransactionResultType())
                    .transactionId(transactionDTO.getTransactionId())
                    .amount(transactionDTO.getAmount())
                    .transactedAt(transactionDTO.getTransactedAt())
                    .build();
        }
    }

}
